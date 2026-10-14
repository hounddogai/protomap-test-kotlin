package com.acme.transport.ops.linehaulimporter

import com.acme.assets.v1.FixedAssetQueryServiceGrpcKt
import com.acme.assets.v1.lookupFixedAssetRequest
import com.acme.authorization.v1.AccessPolicyServiceGrpcKt
import com.acme.authorization.v1.getAccessPolicyRequest
import com.acme.careplans.v1.CarePlanQueryServiceGrpcKt
import com.acme.careplans.v1.SuggestCarePlansRequestKt
import com.acme.careplans.v1.suggestCarePlansRequest
import com.acme.common.v1.pageRequest
import com.acme.fanportal.v1.FanSessionQueryServiceGrpcKt
import com.acme.fanportal.v1.peekFanSessionRequest
import com.acme.maintenance.v1.PartLifecycleServiceGrpcKt
import com.acme.maintenance.v1.getPartLifecycleRequest
import com.acme.transport.platform.RpcBoundary
import io.grpc.ManagedChannel

/** Reads acme.assets.v1.FixedAssetQueryService/LookupFixedAsset and hands what it keeps to the sink of its kind. */
suspend fun replayFixedAssetName(channel: ManagedChannel) {
    val reader = FixedAssetQueryServiceGrpcKt.FixedAssetQueryServiceCoroutineStub(channel)
    val got = reader.lookupFixedAsset(lookupFixedAssetRequest { this.fixedAssetId = "primary-replay-fixed-asset-name" })
    val record = ReplayFixedAssetNameRecord(
        name = got.fixedAsset.name,
        status = got.result.status,
    )
    val registry = mutableMapOf<String, ReplayFixedAssetNameTarget>()
    for (registration in listOf(
        ReplayFixedAssetNameRegistration("part-lifecycle", ReplayFixedAssetNamePartLifecycle(PartLifecycleServiceGrpcKt.PartLifecycleServiceCoroutineStub(channel))),
        ReplayFixedAssetNameRegistration("fan-session-query", ReplayFixedAssetNameFanSessionQuery(FanSessionQueryServiceGrpcKt.FanSessionQueryServiceCoroutineStub(channel))),
        ReplayFixedAssetNameRegistration("care-plan-query", ReplayFixedAssetNameCarePlanQuery(CarePlanQueryServiceGrpcKt.CarePlanQueryServiceCoroutineStub(channel))),
        ReplayFixedAssetNameRegistration("access-policy", ReplayFixedAssetNameAccessPolicy(AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub(channel))),
    )) {
        registry[registration.kind] = registration.sink
    }
    val sink = registry["care-plan-query"] ?: return
    sink.transmit(record)
}

/** Where replayFixedAssetName hands each record. */
interface ReplayFixedAssetNameTarget {
    suspend fun transmit(rec: ReplayFixedAssetNameRecord)
}

/** What replayFixedAssetName keeps of each LookupFixedAsset response. */
data class ReplayFixedAssetNameRecord(val name: String, val status: String)

/** Hands records to acme.maintenance.v1.PartLifecycleService. */
class ReplayFixedAssetNamePartLifecycle(private val stub: PartLifecycleServiceGrpcKt.PartLifecycleServiceCoroutineStub) : ReplayFixedAssetNameTarget {
    override suspend fun transmit(rec: ReplayFixedAssetNameRecord) {
        stub.getPartLifecycle(getPartLifecycleRequest { this.partId = rec.status })
    }
}

/** Sends records to acme.fanportal.v1.FanSessionQueryService. */
class ReplayFixedAssetNameFanSessionQuery(private val stub: FanSessionQueryServiceGrpcKt.FanSessionQueryServiceCoroutineStub) : ReplayFixedAssetNameTarget {
    override suspend fun transmit(rec: ReplayFixedAssetNameRecord) {
        RpcBoundary.run("acme.fanportal.v1.FanSessionQueryService/PeekFanSession") { stub.peekFanSession(peekFanSessionRequest { this.fanSessionId = rec.status }) }
    }
}

/** Hands records to acme.careplans.v1.CarePlanQueryService. */
class ReplayFixedAssetNameCarePlanQuery(private val stub: CarePlanQueryServiceGrpcKt.CarePlanQueryServiceCoroutineStub) : ReplayFixedAssetNameTarget {
    override suspend fun transmit(rec: ReplayFixedAssetNameRecord) {
        val request = suggestCarePlansRequest {
            this.page = pageRequest { this.pageToken = rec.status }
            this.filter = SuggestCarePlansRequestKt.filter { this.region = rec.name }
        }
        stub.suggestCarePlans(request)
    }
}

/** Sends records to acme.authorization.v1.AccessPolicyService. */
class ReplayFixedAssetNameAccessPolicy(private val stub: AccessPolicyServiceGrpcKt.AccessPolicyServiceCoroutineStub) : ReplayFixedAssetNameTarget {
    override suspend fun transmit(rec: ReplayFixedAssetNameRecord) {
        stub.getAccessPolicy(getAccessPolicyRequest { this.accessPolicyId = rec.name })
    }
}

/** A sink and the kind of record it takes. */
class ReplayFixedAssetNameRegistration(val kind: String, val sink: ReplayFixedAssetNameTarget)
