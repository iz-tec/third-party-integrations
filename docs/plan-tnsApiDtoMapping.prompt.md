# Plan: TNS API DTO Mapping

Map all TNS LSM API HTTP contracts to Java Records under `integration-tns`.

## Authentication

**`dto/auth/TnsAuthRequest`**
- `username: String` (@NotBlank)
- `password: String` (@NotBlank)

**`dto/auth/TnsAuthResponse`**
- `token: String`

---

## Invoices — `/partners/invoices/` and `/partners/invoices/<pk>/`

**`dto/invoice/TnsInvoiceResponse`**
- `id: Integer`
- `code: String`
- `customerId: Integer`
- `customerName: String`
- `customerCpfCnpj: String`
- `customerDebt: String`
- `customerOverdue: String`
- `customerBlocked: String`
- `customerTypeName: String`
- `dueDt: String`
- `issueDt: String`
- `subscr: String`
- `recharge: String`
- `excess: String`
- `holdup: String`
- `subtotal: String`
- `leases: String`
- `leasesAdd: String`
- `discount: String`
- `extras: String`
- `total: String`
- `paidAmt: String`
- `lateFees: String`
- `liquidated: Boolean`
- `cancelled: Boolean`
- `edited: String`
- `approved: String`
- `isApproved: String`
- `branchId: Integer`
- `branchName: String`

---

## SIMs — `/partners/sims/` and `/partners/sims/<pk>/`

**`dto/sim/TnsSimResponse`**
- `id: Integer`
- `iccid: String`
- `operadoraId: Integer`
- `msisdn: String`
- `imei: String`
- `imeiLock: Boolean`
- `operatorName: String`
- `typeId: Integer`
- `typeName: String`
- `lineId: Integer`
- `lineTotalF: String`
- `lastConn: String`
- `lastDisc: String`
- `statusId: Integer`
- `statusName: String`
- `soldplanId: Integer`
- `soldplanName: String`
- `soldplanConsumptionF: String`
- `soldplanGroupId: Integer`
- `soldplanGroupName: String`
- `phaseId: Integer`
- `phaseName: String`
- `customerId: Integer`
- `customerName: String`
- `itemId: Integer`
- `contractId: Integer`
- `replacesId: Integer`
- `replacesIccid: String`
- `details: String`

**`dto/sim/TnsSimPatchRequest`** — used for PATCH `/partners/sims/<id>` (block/unblock)
- `phaseId: Integer` — 15 = block (Bloqueio Cliente), 1 = unblock (Vinculado)

---

## Inventory SIMcards — `/partners/inventory/simcards` and `/partners/inventory/simcards/<pk>`

**`dto/inventory/TnsInventorySimcardResponse`**
- `id: Integer`
- `iccid: String`
- `operatorId: Integer`
- `operatorName: String`
- `typeId: Integer`
- `typeName: String`
- `details: String`

---

## Sessions — `/partners/sessions` and `/partners/sessions/<pk>/`

Note: requires at least one filter: `line__id`, `simcard__id`, `line__msisdn` or `simcard__iccid`.

**`dto/session/TnsSessionResponse`**
- `id: Integer`
- `lineId: Integer`
- `simcardId: Integer`
- `operatorId: Integer`
- `msisdn: String`
- `iccid: String`
- `operatorName: String`
- `start: String`
- `end: String`
- `upload: Double`
- `uploadF: String`
- `download: Double`
- `downloadF: String`
- `duration: String`
- `durationF: String`
- `traffic: String`
- `trafficF: String`
- `newImei: String`
- `termcause: String`
- `ipaddr: String`
- `apn: String`
- `mncGgsn: String`
- `mncSgsn: String`
- `userLoc: String`

---

## Async Calls — `/partners/async/calls` and `/partners/async/calls/<pk>`

POST `/partners/async/calls?action=reset&key=<identifier>`
where `<identifier>` is one of: `iccid`, `msisdn`, `line__id`, `simcard__id`.
Payload is a JSON array of string values.

**`dto/async/TnsAsyncCallRequest`**
- `identifiers: List<String>` — array of iccid/msisdn/line__id/simcard__id values

**`dto/async/TnsAsyncCallItemResponse`** — one entry in the POST response array
- `iccid: String`
- `operatorName: String`
- `actionName: String`
- `callId: Integer`
- `lineId: Integer`
- `actionId: Integer`
- `operatorId: Integer`
- `msisdn: String`
- `simcardId: Integer`
- `batchId: Integer`
- `callRequested: String`

**`dto/async/TnsAsyncCallDetailResponse`** — GET `/partners/async/calls/<pk>`
- `id: Integer`
- `actionId: Integer`
- `actionMnemo: String`
- `actionName: String`
- `requested: String`
- `started: String`
- `finished: String`
- `priority: Integer`
- `states: Object`

---

## Async Batches — `/partners/async/batches` and `/partners/async/batches/<pk>`

**`dto/async/TnsAsyncBatchResponse`**
- `id: Integer`
- `callId: Integer`
- `actionId: Integer`
- `actionName: String`
- `started: String`
- `finished: String`
- `stateId: Integer`
- `stateName: String`
- `apiId: Integer`
- `apiUrl: String`
- `operatorId: Integer`
- `operatorName: String`
- `entries: Object`

---

## Orders — `/partners/orders`, `/partners/orders/<pk>`, `/partners/orders/<pk>/lines`, `/partners/orders/<pk>/simcards`

**`dto/order/TnsOrderRequest`** — POST `/partners/orders`
- `typeId: Integer` (@NotNull) — allowed values: 21 (Ativação estoque avançado), 56 (Ativar Suspensão), 60 (Desativar Suspensão)
- `customerId: Integer` (@NotNull)
- `comment: String` (optional)

**`dto/order/TnsOrderResponse`**
- `id: Integer`
- `typeId: Integer`
- `typeName: String`
- `code: String`
- `customerId: Integer`
- `customerName: String`
- `comment: String`
- `statusId: Integer`
- `statusName: String`
- `dttm: String`

**`dto/order/TnsOrderSimcardsRequest`** — PUT `/partners/orders/<pk>/simcards?key=iccid`
- `iccids: List<String>` — list of ICCID strings

---

## Implementation Notes

- All DTOs are Java **Records** (no classes, no Lombok on DTOs).
- JSON field names from the API use `__` (double underscore) as separator (e.g. `customer__id`). Use `@JsonProperty("customer__id")` on each record component to map them correctly.
- `SerializerMethodField` fields from the API are treated as `String` since their exact type depends on TNS server-side formatting.
- Auth header format: `Authorization: JWT <token>`.
- Base URLs per environment:
  - testing: `https://api.lsm-dev.tnsi.com.br/`
  - staging: `https://api.lsm-stg.tnsi.com.br/`
  - production: `https://api.lsm.tnsi.com.br/`
- Token TTL is 300 seconds (5 min) — implement token caching/refresh logic in a `TnsAuthService`.

