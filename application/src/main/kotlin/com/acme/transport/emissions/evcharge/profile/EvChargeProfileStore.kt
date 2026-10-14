package com.acme.transport.emissions.evcharge.profile

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps ev charge profiles by code. */
interface EvChargeProfileStore {
    fun find(code: String): EvChargeProfile?
    fun save(item: EvChargeProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<EvChargeProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEvChargeProfileStore(
    var items: MutableMap<String, EvChargeProfile>,
    var order: MutableList<String>,
) : EvChargeProfileStore {
    override fun find(code: String): EvChargeProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EvChargeProfile) {
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

    override fun list(): MutableList<EvChargeProfile> {
        val out = mutableListOf<EvChargeProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEvChargeProfileStore(): EvChargeProfileStore {
    return MemoryEvChargeProfileStore(mutableMapOf<String, EvChargeProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEvChargeProfileStore(
    var inner: EvChargeProfileStore,
    var cache: TtlCache<EvChargeProfile>,
    var hits: Int,
    var misses: Int,
) : EvChargeProfileStore {
    override fun find(code: String): EvChargeProfile? {
        val hit = (cache.get(code) as EvChargeProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EvChargeProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EvChargeProfile> = inner.list()
}

fun newCachedEvChargeProfileStore(inner: EvChargeProfileStore, ttl: Int, clock: Clock): EvChargeProfileStore {
    return CachedEvChargeProfileStore(
        inner = inner,
        cache = newTtlCache<EvChargeProfile>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects ev charge profiles. Empty fields match all. */
data class EvChargeProfileQuery(
    var locale: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EvChargeProfile): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.minimum < minMinimum) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EvChargeProfile>): MutableList<EvChargeProfile> {
        val matched = filter(items) { candidate: EvChargeProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEvChargeProfiles(store: EvChargeProfileStore, query: EvChargeProfileQuery): MutableList<EvChargeProfile> {
    return query.apply(store.list())
}
