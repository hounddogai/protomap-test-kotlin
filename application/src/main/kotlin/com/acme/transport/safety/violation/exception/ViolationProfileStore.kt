package com.acme.transport.safety.violation.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps violation profiles by code. */
interface ViolationProfileStore {
    fun find(code: String): ViolationProfile?
    fun save(item: ViolationProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<ViolationProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryViolationProfileStore(
    var items: MutableMap<String, ViolationProfile>,
    var order: MutableList<String>,
) : ViolationProfileStore {
    override fun find(code: String): ViolationProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ViolationProfile) {
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

    override fun list(): MutableList<ViolationProfile> {
        val out = mutableListOf<ViolationProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryViolationProfileStore(): ViolationProfileStore {
    return MemoryViolationProfileStore(mutableMapOf<String, ViolationProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedViolationProfileStore(
    var inner: ViolationProfileStore,
    var cache: TtlCache<ViolationProfile>,
    var hits: Int,
    var misses: Int,
) : ViolationProfileStore {
    override fun find(code: String): ViolationProfile? {
        val hit = (cache.get(code) as ViolationProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ViolationProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ViolationProfile> = inner.list()
}

fun newCachedViolationProfileStore(inner: ViolationProfileStore, ttl: Int, clock: Clock): ViolationProfileStore {
    return CachedViolationProfileStore(
        inner = inner,
        cache = newTtlCache<ViolationProfile>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects violation profiles. Empty fields match all. */
data class ViolationProfileQuery(
    var label: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ViolationProfile): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ViolationProfile>): MutableList<ViolationProfile> {
        val matched = filter(items) { candidate: ViolationProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findViolationProfiles(store: ViolationProfileStore, query: ViolationProfileQuery): MutableList<ViolationProfile> {
    return query.apply(store.list())
}
