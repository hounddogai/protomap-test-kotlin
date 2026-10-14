package com.acme.transport.lastmile.proofofdelivery.audit

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery audits by code. */
interface ProofOfDeliveryAuditStore {
    fun find(code: String): ProofOfDeliveryAudit?
    fun save(item: ProofOfDeliveryAudit)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryAudit>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryAuditStore(
    var items: MutableMap<String, ProofOfDeliveryAudit>,
    var order: MutableList<String>,
) : ProofOfDeliveryAuditStore {
    override fun find(code: String): ProofOfDeliveryAudit? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryAudit) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<ProofOfDeliveryAudit> {
        val out = mutableListOf<ProofOfDeliveryAudit>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryAuditStore(): ProofOfDeliveryAuditStore {
    return MemoryProofOfDeliveryAuditStore(
        mutableMapOf<String, ProofOfDeliveryAudit>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryAuditStore(
    var inner: ProofOfDeliveryAuditStore,
    var cache: TtlCache<ProofOfDeliveryAudit>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryAuditStore {
    override fun find(code: String): ProofOfDeliveryAudit? {
        val hit = (cache.get(code) as ProofOfDeliveryAudit?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryAudit) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryAudit> = inner.list()
}

fun newCachedProofOfDeliveryAuditStore(inner: ProofOfDeliveryAuditStore, ttl: Int, clock: Clock): ProofOfDeliveryAuditStore {
    return CachedProofOfDeliveryAuditStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryAudit>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery audits. Empty fields match all. */
data class ProofOfDeliveryAuditQuery(
    var label: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryAudit): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ProofOfDeliveryAudit>): MutableList<ProofOfDeliveryAudit> {
        val matched = filter(items) { candidate: ProofOfDeliveryAudit -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryAudits(store: ProofOfDeliveryAuditStore, query: ProofOfDeliveryAuditQuery): MutableList<ProofOfDeliveryAudit> {
    return query.apply(store.list())
}
