package io.iztec.tp.integration.tns.dto.sim;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.iztec.tp.integration.tns.config.DataSizeFloatDeserializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

/**
 * Represents a single SIM returned by GET /partners/sims/ or GET /partners/sims/<pk>/.
 *
 * _f fields (lineTotalF, soldplanConsumptionF, excessConsumptionF) are sent by TNS as
 * human-readable strings (e.g. "20.00 MB"). {@link DataSizeFloatDeserializer} converts
 * them to a Float value always expressed in bytes.
 */
public record TnsSimResponse(

        // --- identity ---
        @NotNull
        @JsonProperty("id") Integer id,

        @NotBlank
        @JsonProperty("iccid") String iccid,

        @JsonProperty("msisdn") String msisdn,
        @JsonProperty("imei") String imei,
        @JsonProperty("imei_lock") Boolean imeiLock,

        // --- operator / type ---
        @JsonProperty("operadora__id") Integer operadoraId,
        @JsonProperty("operator__name") String operatorName,
        @JsonProperty("type__id") Integer typeId,
        @JsonProperty("type__name") String typeName,

        // --- line ---
        @JsonProperty("line__id") Integer lineId,

        /**
         * Consumed data on this line converted to bytes.
         * TNS sends this as a string (e.g. "0.00 B", "1.50 KB", "20.00 MB").
         */
        @PositiveOrZero
        @JsonDeserialize(using = DataSizeFloatDeserializer.class)
        @JsonProperty("line__total_f") Float lineTotalF,

        @JsonProperty("line__last_suspension") LocalDateTime lineLastSuspension,
        @JsonProperty("line__estimated_suspension_end") LocalDateTime lineEstimatedSuspensionEnd,

        // --- connectivity ---
        @JsonProperty("last_conn") LocalDateTime lastConn,
        @JsonProperty("last_disc") LocalDateTime lastDisc,

        // --- status / phase ---
        @JsonProperty("status__id") Integer statusId,
        @JsonProperty("status__name") String statusName,
        @JsonProperty("phase__id") Integer phaseId,
        @JsonProperty("phase__name") String phaseName,

        // --- sold plan ---
        @JsonProperty("soldplan__id") Integer soldplanId,
        @JsonProperty("soldplan__name") String soldplanName,
        @JsonProperty("soldplan__base_price") Double soldplanBasePrice,

        /**
         * Data quota of the sold plan converted to bytes.
         * TNS sends this as a string (e.g. "20.00 MB").
         */
        @PositiveOrZero
        @JsonDeserialize(using = DataSizeFloatDeserializer.class)
        @JsonProperty("soldplan__consumption_f") Float soldplanConsumptionF,

        @JsonProperty("soldplan__group__id") Integer soldplanGroupId,
        @JsonProperty("soldplan__group__name") String soldplanGroupName,

        // --- customer ---
        @JsonProperty("customer__id") Integer customerId,
        @JsonProperty("customer__name") String customerName,
        @JsonProperty("customer_tid") String customerTid,
        @JsonProperty("customer_mid") String customerMid,
        @JsonProperty("customer_cost_center") String customerCostCenter,

        // --- contract / item ---
        @JsonProperty("item__id") Integer itemId,
        @JsonProperty("contract__id") Integer contractId,
        @JsonProperty("charge_start") LocalDateTime chargeStart,

        // --- replacement SIM ---
        @JsonProperty("replaces__id") Integer replacesId,
        @JsonProperty("replaces__iccid") String replacesIccid,

        // --- extras ---
        @JsonProperty("details") String details,

        /**
         * Data consumed beyond the plan quota converted to bytes.
         * TNS sends this as a string (e.g. "0.00 B").
         */
        @PositiveOrZero
        @JsonDeserialize(using = DataSizeFloatDeserializer.class)
        @JsonProperty("excess_consumption_f") Float excessConsumptionF
) {}

