package com.acme.transport.ops.lastmilebackfill

import com.acme.customers.v1.CustomerCalculationServiceGrpcKt
import com.acme.customers.v1.simulateCustomerRequest
import com.acme.driverapp.v1.DriverHomeApprovalServiceGrpcKt
import com.acme.driverapp.v1.rejectDriverHomeRequest
import com.acme.drivers.v1.DriverLifecycleServiceGrpcKt
import com.acme.drivers.v1.terminateDriverRequest
import com.acme.transport.kit.text.normalize
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.YardMoveApprovalServiceGrpcKt
import com.acme.yards.v1.recallYardMoveRequest
import io.grpc.ManagedChannel

/** Reads acme.customers.v1.CustomerCalculationService/SimulateCustomer and hands what it keeps to the sink of its kind. */
suspend fun routeCustomerId(channel: ManagedChannel) {
    val source = CustomerCalculationServiceGrpcKt.CustomerCalculationServiceCoroutineStub(channel)
    val found = source.simulateCustomer(simulateCustomerRequest { this.customerId = "default-route-customer-id" })
    val rec = RouteCustomerIdRecord(
        customerId = found.customer.customerId,
        message = normalize(found.result.message),
    )
    val sinks = listOf<RouteCustomerIdSink>(
        RouteCustomerIdDriverLifecycle(DriverLifecycleServiceGrpcKt.DriverLifecycleServiceCoroutineStub(channel)),
        RouteCustomerIdYardMoveApproval(YardMoveApprovalServiceGrpcKt.YardMoveApprovalServiceCoroutineStub(channel)),
        RouteCustomerIdDriverHomeApproval(DriverHomeApprovalServiceGrpcKt.DriverHomeApprovalServiceCoroutineStub(channel)),
    )
    sinks[2].transmit(rec)
}

/** Where routeCustomerId hands each record. */
interface RouteCustomerIdSink {
    suspend fun transmit(rec: RouteCustomerIdRecord)
}

/** What routeCustomerId keeps of each SimulateCustomer response. */
data class RouteCustomerIdRecord(val customerId: String, val message: String)

/** Sends records to acme.drivers.v1.DriverLifecycleService. */
class RouteCustomerIdDriverLifecycle(private val stub: DriverLifecycleServiceGrpcKt.DriverLifecycleServiceCoroutineStub) : RouteCustomerIdSink {
    override suspend fun transmit(rec: RouteCustomerIdRecord) {
        stub.terminateDriver(terminateDriverRequest { this.driverId = rec.customerId })
    }
}

/** Writes records to acme.yards.v1.YardMoveApprovalService. */
class RouteCustomerIdYardMoveApproval(private val stub: YardMoveApprovalServiceGrpcKt.YardMoveApprovalServiceCoroutineStub) : RouteCustomerIdSink {
    override suspend fun transmit(rec: RouteCustomerIdRecord) {
        RpcBoundary.run("acme.yards.v1.YardMoveApprovalService/RecallYardMove") { stub.recallYardMove(recallYardMoveRequest { this.reason = rec.customerId }) }
    }
}

/** Sends records to acme.driverapp.v1.DriverHomeApprovalService. */
class RouteCustomerIdDriverHomeApproval(private val stub: DriverHomeApprovalServiceGrpcKt.DriverHomeApprovalServiceCoroutineStub) : RouteCustomerIdSink {
    override suspend fun transmit(rec: RouteCustomerIdRecord) {
        stub.rejectDriverHome(rejectDriverHomeRequest { this.reason = rec.customerId; this.driverHomeId = rec.message })
    }
}
