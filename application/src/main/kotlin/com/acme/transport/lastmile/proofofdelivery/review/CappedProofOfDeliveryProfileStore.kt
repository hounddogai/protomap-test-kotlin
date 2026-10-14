package com.acme.transport.lastmile.proofofdelivery.review

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps capped proof of delivery profiles by code. */
interface CappedProofOfDeliveryProfileStore {
    fun find(code: String): CappedProofOfDeliveryProfile?
    fun save(item: CappedProofOfDeliveryProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<CappedProofOfDeliveryProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCappedProofOfDeliveryProfileStore(
    var items: MutableMap<String, CappedProofOfDeliveryProfile>,
    var order: MutableList<String>,
) : CappedProofOfDeliveryProfileStore {
    override fun find(code: String): CappedProofOfDeliveryProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CappedProofOfDeliveryProfile) {
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

    override fun list(): MutableList<CappedProofOfDeliveryProfile> {
        val out = mutableListOf<CappedProofOfDeliveryProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCappedProofOfDeliveryProfileStore(): CappedProofOfDeliveryProfileStore {
    return MemoryCappedProofOfDeliveryProfileStore(
        mutableMapOf<String, CappedProofOfDeliveryProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCappedProofOfDeliveryProfileStore(
    var inner: CappedProofOfDeliveryProfileStore,
    var cache: TtlCache<CappedProofOfDeliveryProfile>,
    var hits: Int,
    var misses: Int,
) : CappedProofOfDeliveryProfileStore {
    override fun find(code: String): CappedProofOfDeliveryProfile? {
        val hit = (cache.get(code) as CappedProofOfDeliveryProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CappedProofOfDeliveryProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CappedProofOfDeliveryProfile> = inner.list()
}

fun newCachedCappedProofOfDeliveryProfileStore(inner: CappedProofOfDeliveryProfileStore, ttl: Int, clock: Clock): CappedProofOfDeliveryProfileStore {
    return CachedCappedProofOfDeliveryProfileStore(
        inner = inner,
        cache = newTtlCache<CappedProofOfDeliveryProfile>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects capped proof of delivery profiles. Empty fields match all. */
data class CappedProofOfDeliveryProfileQuery(
    var name: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CappedProofOfDeliveryProfile): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<CappedProofOfDeliveryProfile>): MutableList<CappedProofOfDeliveryProfile> {
        val matched = filter(items) { candidate: CappedProofOfDeliveryProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCappedProofOfDeliveryProfiles(store: CappedProofOfDeliveryProfileStore, query: CappedProofOfDeliveryProfileQuery): MutableList<CappedProofOfDeliveryProfile> {
    return query.apply(store.list())
}
