package com.acme.transport.lastmile.proofofdelivery.budget

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery baselines by code. */
interface ProofOfDeliveryBaselineStore {
    fun find(code: String): ProofOfDeliveryBaseline?
    fun save(item: ProofOfDeliveryBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryBaselineStore(
    var items: MutableMap<String, ProofOfDeliveryBaseline>,
    var order: MutableList<String>,
) : ProofOfDeliveryBaselineStore {
    override fun find(code: String): ProofOfDeliveryBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryBaseline) {
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

    override fun list(): MutableList<ProofOfDeliveryBaseline> {
        val out = mutableListOf<ProofOfDeliveryBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryBaselineStore(): ProofOfDeliveryBaselineStore {
    return MemoryProofOfDeliveryBaselineStore(
        mutableMapOf<String, ProofOfDeliveryBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryBaselineStore(
    var inner: ProofOfDeliveryBaselineStore,
    var cache: TtlCache<ProofOfDeliveryBaseline>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryBaselineStore {
    override fun find(code: String): ProofOfDeliveryBaseline? {
        val hit = (cache.get(code) as ProofOfDeliveryBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryBaseline> = inner.list()
}

fun newCachedProofOfDeliveryBaselineStore(inner: ProofOfDeliveryBaselineStore, ttl: Int, clock: Clock): ProofOfDeliveryBaselineStore {
    return CachedProofOfDeliveryBaselineStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryBaseline>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery baselines. Empty fields match all. */
data class ProofOfDeliveryBaselineQuery(
    var label: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryBaseline): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.windowDays < minWindowDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ProofOfDeliveryBaseline>): MutableList<ProofOfDeliveryBaseline> {
        val matched = filter(items) { candidate: ProofOfDeliveryBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryBaselines(store: ProofOfDeliveryBaselineStore, query: ProofOfDeliveryBaselineQuery): MutableList<ProofOfDeliveryBaseline> {
    return query.apply(store.list())
}
