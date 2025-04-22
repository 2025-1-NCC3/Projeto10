const express = require("express");
const mysql = require("mysql2");
const cors = require("cors");
const bcrypt = require("bcrypt");
const app = express();
const port = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

const connection = mysql.createConnection({
  host: "ubergirlsmysqlgrupo10.mysql.database.azure.com",
  user: "admingrupo10@ubergirlsmysqlgrupo10",
  password: "Grupo10@",
  database: "ubergirlsdb",
  ssl: { rejectUnauthorized: false }
});

connection.connect(err => {
  if (err) {
    console.error("Erro ao conectar no MySQL:", err);
    return;
  }
  console.log("Conectado ao MySQL com sucesso!");
});

app.get("/usuarios", (req, res) => {
  connection.query("SELECT * FROM users", (err, results) => {
    if (err) {
      return res.status(500).json({ error: err });
    }
    res.json(results);
  });
});

app.post("/cadastro", async (req, res) => {
  const { nome, telefone, email, cpf, senha, sexo, aceita_motorista_mulher } = req.body;


  if (!nome || !telefone || !email || !cpf || !senha || !sexo) {
    return res.status(400).json({ error: "Todos os campos são obrigatórios." });
  }


  if (sexo.toLowerCase() === "masculino" && aceita_motorista_mulher === 1) {
    return res.status(403).json({
      error: "Homens não podem selecionar a opção 'aceita motorista mulher'."
    });
  }

  try {
    const senhaCriptografada = await bcrypt.hash(senha, 10);

    const query = `
      INSERT INTO users (nome, telefone, email, cpf, senha, sexo, aceita_motorista_mulher)
      VALUES (?, ?, ?, ?, ?, ?, ?)
    `;
    const values = [nome, telefone, email, cpf, senhaCriptografada, sexo, aceita_motorista_mulher];

    connection.query(query, values, (err, results) => {
      if (err) {
        return res.status(500).json({ error: "Erro ao cadastrar usuário", detalhes: err });
      }
      res.status(201).json({ mensagem: "Usuário cadastrado com sucesso!" });
    });
  } catch (error) {
    res.status(500).json({ error: "Erro interno ao criptografar senha" });
  }
});


app.post("/login", (req, res) => {
  const { nome, senha } = req.body;

  const query = "SELECT * FROM users WHERE nome = ?";
  connection.query(query, [nome], async (err, results) => {
    if (err || results.length === 0) {
      return res.status(401).json({ error: "Usuário não encontrado" });
    }

    const usuario = results[0];
    const senhaCorreta = await bcrypt.compare(senha, usuario.senha);

    if (senhaCorreta) {
      res.status(200).json({ mensagem: "Login bem-sucedido", usuario });
    } else {
      res.status(401).json({ error: "Senha incorreta" });
    }
  });
});

app.get("/", (_, res) => {
  res.send("API ESTÁ RODANDO PROFS S2");
});

app.listen(port, () => {
  console.log(`Servidor rodando na porta ${port}`);
});


