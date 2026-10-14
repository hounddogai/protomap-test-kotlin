package com.acme.transport.ops.freightpayreplicator

import com.acme.catalog.v1.ProductCalculationServiceGrpcKt
import com.acme.catalog.v1.calculateProductRequest
import com.acme.deliveries.v1.DeliveryExceptionValidationServiceGrpcKt
import com.acme.deliveries.v1.deliveryException
import com.acme.deliveries.v1.validateDeliveryExceptionRequest
import com.acme.expenses.v1.CalculateExpenseReportRequestKt
import com.acme.expenses.v1.ExpenseReportCalculationServiceGrpcKt
import com.acme.expenses.v1.calculateExpenseReportRequest
import com.acme.expenses.v1.expenseReport
import com.acme.maintenance.v1.ResumeWorkOrderWorkflowRequestKt
import com.acme.maintenance.v1.WorkOrderWorkflowServiceGrpcKt
import com.acme.maintenance.v1.resumeWorkOrderWorkflowRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

/** Reads acme.catalog.v1.ProductCalculationService/CalculateProduct and hands what it keeps to the sink of its kind. */
suspend fun propagateProductCode(channel: ManagedChannel) {
    val reader = ProductCalculationServiceGrpcKt.ProductCalculationServiceCoroutineStub(channel)
    val found = reader.calculateProduct(calculateProductRequest { this.reason = "primary-propagate-product-code" })
    val row = PropagateProductCodeRecord(
        currencyCode = found.breakdown.currencyCode,
        message = found.result.message,
        code = found.product.code,
        name = found.product.name,
    )
    deliverPropagateProductCode("expense-report-calculation", row, channel)
}

/** Where propagateProductCode hands each record. */
interface PropagateProductCodeDestination {
    suspend fun hand(rec: PropagateProductCodeRecord)
}

/** What propagateProductCode keeps of each CalculateProduct response. */
data class PropagateProductCodeRecord(val currencyCode: String, val message: String, val code: String, val name: String)

/** Sends records to acme.deliveries.v1.DeliveryExceptionValidationService. */
class PropagateProductCodeDeliveryExceptionValidation(private val stub: DeliveryExceptionValidationServiceGrpcKt.DeliveryExceptionValidationServiceCoroutineStub) : PropagateProductCodeDestination {
    override suspend fun hand(rec: PropagateProductCodeRecord) {
        val request = validateDeliveryExceptionRequest {
            this.deliveryException = deliveryException { this.deliveryExceptionId = rec.name }
        }
        stub.validateDeliveryException(request)
    }
}

/** Sends records to acme.expenses.v1.ExpenseReportCalculationService. */
class PropagateProductCodeExpenseReportCalculation(private val stub: ExpenseReportCalculationServiceGrpcKt.ExpenseReportCalculationServiceCoroutineStub) : PropagateProductCodeDestination {
    override suspend fun hand(rec: PropagateProductCodeRecord) {
        val request = calculateExpenseReportRequest {
            this.expenseReport = expenseReport { this.number = rec.message }
            this.details = CalculateExpenseReportRequestKt.details { this.currencyCode = rec.currencyCode }
        }
        RpcBoundary.run("acme.expenses.v1.ExpenseReportCalculationService/CalculateExpenseReport") { stub.calculateExpenseReport(request) }
    }
}

/** Writes records to acme.maintenance.v1.WorkOrderWorkflowService. */
class PropagateProductCodeWorkOrderWorkflow(private val stub: WorkOrderWorkflowServiceGrpcKt.WorkOrderWorkflowServiceCoroutineStub) : PropagateProductCodeDestination {
    override suspend fun hand(rec: PropagateProductCodeRecord) {
        val request = resumeWorkOrderWorkflowRequest {
            this.details = ResumeWorkOrderWorkflowRequestKt.details { this.step = rec.code }
            this.reason = rec.currencyCode
            this.workOrderId = rec.name
        }
        stub.resumeWorkOrderWorkflow(request)
    }
}

/** Only logs, for kinds no service takes yet. */
class PropagateProductCodeLogOnly : PropagateProductCodeDestination {
    override suspend fun hand(rec: PropagateProductCodeRecord) {
        LoggerFactory.getLogger("PropagateProductCodeRecord").atInfo().addKeyValue("currencyCode", rec.currencyCode).log("No sink for record")
    }
}

/** Hands a record to the sink of its kind. */
suspend fun deliverPropagateProductCode(kind: String, rec: PropagateProductCodeRecord, channel: ManagedChannel) {
    var sink: PropagateProductCodeDestination = PropagateProductCodeLogOnly()
    when (kind) {
        "delivery-exception-validation" -> sink = PropagateProductCodeDeliveryExceptionValidation(DeliveryExceptionValidationServiceGrpcKt.DeliveryExceptionValidationServiceCoroutineStub(channel))
        "expense-report-calculation" -> sink = PropagateProductCodeExpenseReportCalculation(ExpenseReportCalculationServiceGrpcKt.ExpenseReportCalculationServiceCoroutineStub(channel))
        "work-order-workflow" -> sink = PropagateProductCodeWorkOrderWorkflow(WorkOrderWorkflowServiceGrpcKt.WorkOrderWorkflowServiceCoroutineStub(channel))
    }
    sink.hand(rec)
}
