package com.acme.transport.lastmile.proofofdelivery.deliveryreview

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps global proof of delivery overrides by code. */
interface GlobalProofOfDeliveryOverrideStore {
    fun find(code: String): GlobalProofOfDeliveryOverride?
    fun save(item: GlobalProofOfDeliveryOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<GlobalProofOfDeliveryOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryGlobalProofOfDeliveryOverrideStore(
    var items: MutableMap<String, GlobalProofOfDeliveryOverride>,
    var order: MutableList<String>,
) : GlobalProofOfDeliveryOverrideStore {
    override fun find(code: String): GlobalProofOfDeliveryOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: GlobalProofOfDeliveryOverride) {
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

    override fun list(): MutableList<GlobalProofOfDeliveryOverride> {
        val out = mutableListOf<GlobalProofOfDeliveryOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryGlobalProofOfDeliveryOverrideStore(): GlobalProofOfDeliveryOverrideStore {
    return MemoryGlobalProofOfDeliveryOverrideStore(
        mutableMapOf<String, GlobalProofOfDeliveryOverride>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedGlobalProofOfDeliveryOverrideStore(
    var inner: GlobalProofOfDeliveryOverrideStore,
    var cache: TtlCache<GlobalProofOfDeliveryOverride>,
    var hits: Int,
    var misses: Int,
) : GlobalProofOfDeliveryOverrideStore {
    override fun find(code: String): GlobalProofOfDeliveryOverride? {
        val hit = (cache.get(code) as GlobalProofOfDeliveryOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: GlobalProofOfDeliveryOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<GlobalProofOfDeliveryOverride> = inner.list()
}

fun newCachedGlobalProofOfDeliveryOverrideStore(inner: GlobalProofOfDeliveryOverrideStore, ttl: Int, clock: Clock): GlobalProofOfDeliveryOverrideStore {
    return CachedGlobalProofOfDeliveryOverrideStore(
        inner = inner,
        cache = newTtlCache<GlobalProofOfDeliveryOverride>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects global proof of delivery overrides. Empty fields match all. */
data class GlobalProofOfDeliveryOverrideQuery(
    var name: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: GlobalProofOfDeliveryOverride): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<GlobalProofOfDeliveryOverride>): MutableList<GlobalProofOfDeliveryOverride> {
        val matched = filter(items) { candidate: GlobalProofOfDeliveryOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findGlobalProofOfDeliveryOverrides(store: GlobalProofOfDeliveryOverrideStore, query: GlobalProofOfDeliveryOverrideQuery): MutableList<GlobalProofOfDeliveryOverride> {
    return query.apply(store.list())
}
