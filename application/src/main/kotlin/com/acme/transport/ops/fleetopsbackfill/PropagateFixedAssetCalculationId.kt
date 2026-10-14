package com.acme.transport.ops.fleetopsbackfill

import com.acme.assets.v1.FixedAssetCalculationServiceGrpcKt
import com.acme.assets.v1.explainFixedAssetCalculationRequest
import com.acme.contracts.v1.ContractServiceGrpcKt
import com.acme.contracts.v1.getContractRequest
import com.acme.customers.v1.HouseholdApprovalServiceGrpcKt
import com.acme.customers.v1.commentHouseholdApprovalRequest
import com.acme.customers.v1.household
import com.acme.driverapp.v1.DriverHomeQueryServiceGrpcKt
import com.acme.driverapp.v1.peekDriverHomeRequest
import com.acme.fuel.v1.FuelPolicyCalculationServiceGrpcKt
import com.acme.fuel.v1.explainFuelPolicyCalculationRequest
import com.acme.fuel.v1.fuelPolicy
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.assets.v1.FixedAssetCalculationService/ExplainFixedAssetCalculation and hands what it keeps to the sink of its kind. */
suspend fun propagateFixedAssetCalculationId(channel: ManagedChannel) {
    val upstream = FixedAssetCalculationServiceGrpcKt.FixedAssetCalculationServiceCoroutineStub(channel)
    val got = upstream.explainFixedAssetCalculation(explainFixedAssetCalculationRequest { this.fixedAssetId = "primary-propagate-fixed-asset-calculation-id" })
    val entry = PropagateFixedAssetCalculationIdRecord(
        fixedAssetId = normalize(got.fixedAsset.fixedAssetId),
        status = got.result.status,
        outcomeStatus = got.outcome.status,
        message = got.result.message,
    )
    for (sink in listOf<PropagateFixedAssetCalculationIdDestination>(
        PropagateFixedAssetCalculationIdDriverHomeQuery(DriverHomeQueryServiceGrpcKt.DriverHomeQueryServiceCoroutineStub(channel)),
        PropagateFixedAssetCalculationIdContract(ContractServiceGrpcKt.ContractServiceCoroutineStub(channel)),
        PropagateFixedAssetCalculationIdHouseholdApproval(HouseholdApprovalServiceGrpcKt.HouseholdApprovalServiceCoroutineStub(channel)),
        PropagateFixedAssetCalculationIdFuelPolicyCalculation(FuelPolicyCalculationServiceGrpcKt.FuelPolicyCalculationServiceCoroutineStub(channel)),
    )) {
        sink.hand(entry)
    }
}

/** Where propagateFixedAssetCalculationId hands each record. */
interface PropagateFixedAssetCalculationIdDestination {
    suspend fun hand(rec: PropagateFixedAssetCalculationIdRecord)
}

/** What propagateFixedAssetCalculationId keeps of each ExplainFixedAssetCalculation response. */
data class PropagateFixedAssetCalculationIdRecord(val fixedAssetId: String, val status: String, val outcomeStatus: String, val message: String)

/** Sends records to acme.driverapp.v1.DriverHomeQueryService. */
class PropagateFixedAssetCalculationIdDriverHomeQuery(private val stub: DriverHomeQueryServiceGrpcKt.DriverHomeQueryServiceCoroutineStub) : PropagateFixedAssetCalculationIdDestination {
    override suspend fun hand(rec: PropagateFixedAssetCalculationIdRecord) {
        RpcBoundary.run("acme.driverapp.v1.DriverHomeQueryService/PeekDriverHome") { stub.peekDriverHome(peekDriverHomeRequest { this.driverHomeId = rec.fixedAssetId }) }
    }
}

/** Passes records to acme.contracts.v1.ContractService. */
class PropagateFixedAssetCalculationIdContract(private val stub: ContractServiceGrpcKt.ContractServiceCoroutineStub) : PropagateFixedAssetCalculationIdDestination {
    override suspend fun hand(rec: PropagateFixedAssetCalculationIdRecord) {
        stub.getContract(getContractRequest { this.contractId = rec.fixedAssetId })
    }
}

/** Sends records to acme.customers.v1.HouseholdApprovalService. */
class PropagateFixedAssetCalculationIdHouseholdApproval(private val stub: HouseholdApprovalServiceGrpcKt.HouseholdApprovalServiceCoroutineStub) : PropagateFixedAssetCalculationIdDestination {
    override suspend fun hand(rec: PropagateFixedAssetCalculationIdRecord) {
        val request = commentHouseholdApprovalRequest {
            this.household = household { this.parentId = rec.outcomeStatus }
            this.householdId = rec.status
        }
        stub.commentHouseholdApproval(request)
    }
}

/** Sends records to acme.fuel.v1.FuelPolicyCalculationService. */
class PropagateFixedAssetCalculationIdFuelPolicyCalculation(private val stub: FuelPolicyCalculationServiceGrpcKt.FuelPolicyCalculationServiceCoroutineStub) : PropagateFixedAssetCalculationIdDestination {
    override suspend fun hand(rec: PropagateFixedAssetCalculationIdRecord) {
        val request = explainFuelPolicyCalculationRequest {
            this.reason = rec.outcomeStatus
            this.fuelPolicy = fuelPolicy { this.fuelPolicyId = rec.message }
            this.fuelPolicyId = rec.status
        }
        stub.explainFuelPolicyCalculation(request)
    }
}
