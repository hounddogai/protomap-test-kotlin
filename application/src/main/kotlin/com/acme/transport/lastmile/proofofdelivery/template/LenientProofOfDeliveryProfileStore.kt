package com.acme.transport.lastmile.proofofdelivery.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps lenient proof of delivery profiles by code. */
interface LenientProofOfDeliveryProfileStore {
    fun find(code: String): LenientProofOfDeliveryProfile?
    fun save(item: LenientProofOfDeliveryProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientProofOfDeliveryProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientProofOfDeliveryProfileStore(
    var items: MutableMap<String, LenientProofOfDeliveryProfile>,
    var order: MutableList<String>,
) : LenientProofOfDeliveryProfileStore {
    override fun find(code: String): LenientProofOfDeliveryProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientProofOfDeliveryProfile) {
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

    override fun list(): MutableList<LenientProofOfDeliveryProfile> {
        val out = mutableListOf<LenientProofOfDeliveryProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientProofOfDeliveryProfileStore(): LenientProofOfDeliveryProfileStore {
    return MemoryLenientProofOfDeliveryProfileStore(
        mutableMapOf<String, LenientProofOfDeliveryProfile>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientProofOfDeliveryProfileStore(
    var inner: LenientProofOfDeliveryProfileStore,
    var cache: TtlCache<LenientProofOfDeliveryProfile>,
    var hits: Int,
    var misses: Int,
) : LenientProofOfDeliveryProfileStore {
    override fun find(code: String): LenientProofOfDeliveryProfile? {
        val hit = (cache.get(code) as LenientProofOfDeliveryProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientProofOfDeliveryProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientProofOfDeliveryProfile> = inner.list()
}

fun newCachedLenientProofOfDeliveryProfileStore(inner: LenientProofOfDeliveryProfileStore, ttl: Int, clock: Clock): LenientProofOfDeliveryProfileStore {
    return CachedLenientProofOfDeliveryProfileStore(
        inner = inner,
        cache = newTtlCache<LenientProofOfDeliveryProfile>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient proof of delivery profiles. Empty fields match all. */
data class LenientProofOfDeliveryProfileQuery(
    var region: String,
    var minGraceDays: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientProofOfDeliveryProfile): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
            return false
        }
        if (item.graceDays < minGraceDays) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientProofOfDeliveryProfile>): MutableList<LenientProofOfDeliveryProfile> {
        val matched = filter(items) { candidate: LenientProofOfDeliveryProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientProofOfDeliveryProfiles(store: LenientProofOfDeliveryProfileStore, query: LenientProofOfDeliveryProfileQuery): MutableList<LenientProofOfDeliveryProfile> {
    return query.apply(store.list())
}
