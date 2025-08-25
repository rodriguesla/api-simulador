import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  vus: 20,
  duration: '60s',
};

const BASE_URL = 'http://localhost:8080';
const TOKEN = 'eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJoYWNrYXRob24tMjAyNSIsImlzcyI6ImFwaS1zaW11bGFkb3IiLCJzdWIiOiJoYWNrYXRob24iLCJpYXQiOjE3NTYxNjIyMDMsImV4cCI6MTc1NjE2NTgwM30.MwM4Us9cAaCWz11MISPuz0H9mFoe-SClyPe8EwnooVc';

const authHeader = {
  headers: {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${TOKEN}`
  }
};

export default function () {

  // /simulacoes POST
  const simulacaoPayload = JSON.stringify({
    valorDesejado: 10000,
    prazo: 12,
  });
  let simulacaoRes = http.post(`${BASE_URL}/simulacoes`, simulacaoPayload, authHeader);
  check(simulacaoRes, { 'simulacoes status is 200': (r) => r.status === 200 });

  // /simulacoes GET
  let listaRes = http.get(`${BASE_URL}/simulacoes?pagina=0&qtdRegistrosPagina=10`, authHeader);
  check(listaRes, { 'listarSimulacoes status is 200': (r) => r.status === 200 });

  // /simulacoes/por-dia GET
  let porDiaRes = http.get(`${BASE_URL}/simulacoes/por-dia?data=2025-08-25`, authHeader);
  check(porDiaRes, { 'simulacoesPorDia status is 200': (r) => r.status === 200 });

  // /simulacoes/telemetria GET
  let telemetriaRes = http.get(`${BASE_URL}/simulacoes/telemetria`, authHeader);
  check(telemetriaRes, { 'telemetria status is 200': (r) => r.status === 200 });

  sleep(1);
}