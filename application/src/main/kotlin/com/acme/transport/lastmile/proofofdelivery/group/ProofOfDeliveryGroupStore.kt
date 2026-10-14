package com.acme.transport.lastmile.proofofdelivery.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps proof of delivery groups by code. */
interface ProofOfDeliveryGroupStore {
    fun find(code: String): ProofOfDeliveryGroup?
    fun save(item: ProofOfDeliveryGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<ProofOfDeliveryGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryProofOfDeliveryGroupStore(
    var items: MutableMap<String, ProofOfDeliveryGroup>,
    var order: MutableList<String>,
) : ProofOfDeliveryGroupStore {
    override fun find(code: String): ProofOfDeliveryGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ProofOfDeliveryGroup) {
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

    override fun list(): MutableList<ProofOfDeliveryGroup> {
        val out = mutableListOf<ProofOfDeliveryGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryProofOfDeliveryGroupStore(): ProofOfDeliveryGroupStore {
    return MemoryProofOfDeliveryGroupStore(
        mutableMapOf<String, ProofOfDeliveryGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedProofOfDeliveryGroupStore(
    var inner: ProofOfDeliveryGroupStore,
    var cache: TtlCache<ProofOfDeliveryGroup>,
    var hits: Int,
    var misses: Int,
) : ProofOfDeliveryGroupStore {
    override fun find(code: String): ProofOfDeliveryGroup? {
        val hit = (cache.get(code) as ProofOfDeliveryGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ProofOfDeliveryGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ProofOfDeliveryGroup> = inner.list()
}

fun newCachedProofOfDeliveryGroupStore(inner: ProofOfDeliveryGroupStore, ttl: Int, clock: Clock): ProofOfDeliveryGroupStore {
    return CachedProofOfDeliveryGroupStore(
        inner = inner,
        cache = newTtlCache<ProofOfDeliveryGroup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects proof of delivery groups. Empty fields match all. */
data class ProofOfDeliveryGroupQuery(
    var lane: String,
    var minWindowDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ProofOfDeliveryGroup): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
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

    fun apply(items: MutableList<ProofOfDeliveryGroup>): MutableList<ProofOfDeliveryGroup> {
        val matched = filter(items) { candidate: ProofOfDeliveryGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findProofOfDeliveryGroups(store: ProofOfDeliveryGroupStore, query: ProofOfDeliveryGroupQuery): MutableList<ProofOfDeliveryGroup> {
    return query.apply(store.list())
}
