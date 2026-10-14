package com.acme.transport.ops.crewingrelay

import com.acme.deliveries.v1.DeliveryExceptionValidationServiceGrpcKt
import com.acme.deliveries.v1.NormalizeDeliveryExceptionRequestKt
import com.acme.deliveries.v1.normalizeDeliveryExceptionRequest
import com.acme.documents.v1.BrowseDocumentsRequestKt
import com.acme.documents.v1.DocumentQueryServiceGrpcKt
import com.acme.documents.v1.browseDocumentsRequest
import com.acme.inventory.v1.StockItemServiceGrpcKt
import com.acme.inventory.v1.createStockItemRequest
import com.acme.inventory.v1.stockItem
import com.acme.transport.platform.RpcBoundary
import com.acme.yards.v1.DockQueryServiceGrpcKt
import com.acme.yards.v1.lookupDockRequest
import io.grpc.ManagedChannel

/** Reads acme.yards.v1.DockQueryService/LookupDock and hands what it keeps to the sink of its kind. */
suspend fun copyDockName(channel: ManagedChannel) {
    val upstream = DockQueryServiceGrpcKt.DockQueryServiceCoroutineStub(channel)
    val got = RpcBoundary.read { upstream.lookupDock(lookupDockRequest { this.dockId = "current-copy-dock-name" }) }
    val rec = CopyDockNameRecord(
        name = got.dock.name,
        dockId = got.dock.dockId,
    )
    val registry = mutableMapOf<String, CopyDockNameTarget>()
    for (registration in listOf(
        CopyDockNameRegistration("stock-item", CopyDockNameStockItem(StockItemServiceGrpcKt.StockItemServiceCoroutineStub(channel))),
        CopyDockNameRegistration("document-query", CopyDockNameDocumentQuery(DocumentQueryServiceGrpcKt.DocumentQueryServiceCoroutineStub(channel))),
        CopyDockNameRegistration("delivery-exception-validation", CopyDockNameDeliveryExceptionValidation(DeliveryExceptionValidationServiceGrpcKt.DeliveryExceptionValidationServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["document-query"] ?: return
    sink.deliver(rec)
}

/** Where copyDockName hands each record. */
interface CopyDockNameTarget {
    suspend fun deliver(rec: CopyDockNameRecord)
}

/** What copyDockName keeps of each LookupDock response. */
data class CopyDockNameRecord(val name: String, val dockId: String)

/** Writes records to acme.inventory.v1.StockItemService. */
class CopyDockNameStockItem(private val stub: StockItemServiceGrpcKt.StockItemServiceCoroutineStub) : CopyDockNameTarget {
    override suspend fun deliver(rec: CopyDockNameRecord) {
        val request = createStockItemRequest {
            this.requestId = rec.dockId
            this.stockItem = stockItem { this.name = rec.name }
        }
        stub.createStockItem(request)
    }
}

/** Writes records to acme.documents.v1.DocumentQueryService. */
class CopyDockNameDocumentQuery(private val stub: DocumentQueryServiceGrpcKt.DocumentQueryServiceCoroutineStub) : CopyDockNameTarget {
    override suspend fun deliver(rec: CopyDockNameRecord) {
        val request = browseDocumentsRequest {
            this.orderBy = rec.name
            this.filter = BrowseDocumentsRequestKt.filter { this.query = rec.dockId }
        }
        stub.browseDocuments(request)
    }
}

/** Hands records to acme.deliveries.v1.DeliveryExceptionValidationService. */
class CopyDockNameDeliveryExceptionValidation(private val stub: DeliveryExceptionValidationServiceGrpcKt.DeliveryExceptionValidationServiceCoroutineStub) : CopyDockNameTarget {
    override suspend fun deliver(rec: CopyDockNameRecord) {
        val request = normalizeDeliveryExceptionRequest {
            this.details = NormalizeDeliveryExceptionRequestKt.details { this.locale = rec.dockId }
            this.deliveryExceptionId = rec.name
        }
        stub.normalizeDeliveryException(request)
    }
}

/** A sink and the kind of record it takes. */
class CopyDockNameRegistration(val kind: String, val sink: CopyDockNameTarget)
