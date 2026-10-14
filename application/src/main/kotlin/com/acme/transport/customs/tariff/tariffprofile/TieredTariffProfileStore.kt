package com.acme.transport.customs.tariff.tariffprofile

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps tiered tariff profiles by code. */
interface TieredTariffProfileStore {
    fun find(code: String): TieredTariffProfile?
    fun save(item: TieredTariffProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<TieredTariffProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTieredTariffProfileStore(
    var items: MutableMap<String, TieredTariffProfile>,
    var order: MutableList<String>,
) : TieredTariffProfileStore {
    override fun find(code: String): TieredTariffProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TieredTariffProfile) {
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

    override fun list(): MutableList<TieredTariffProfile> {
        val out = mutableListOf<TieredTariffProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTieredTariffProfileStore(): TieredTariffProfileStore {
    return MemoryTieredTariffProfileStore(mutableMapOf<String, TieredTariffProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTieredTariffProfileStore(
    var inner: TieredTariffProfileStore,
    var cache: TtlCache<TieredTariffProfile>,
    var hits: Int,
    var misses: Int,
) : TieredTariffProfileStore {
    override fun find(code: String): TieredTariffProfile? {
        val hit = (cache.get(code) as TieredTariffProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TieredTariffProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TieredTariffProfile> = inner.list()
}

fun newCachedTieredTariffProfileStore(inner: TieredTariffProfileStore, ttl: Int, clock: Clock): TieredTariffProfileStore {
    return CachedTieredTariffProfileStore(
        inner = inner,
        cache = newTtlCache<TieredTariffProfile>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects tiered tariff profiles. Empty fields match all. */
data class TieredTariffProfileQuery(
    var locale: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TieredTariffProfile): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.etaMinutes < minEtaMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TieredTariffProfile>): MutableList<TieredTariffProfile> {
        val matched = filter(items) { candidate: TieredTariffProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTieredTariffProfiles(store: TieredTariffProfileStore, query: TieredTariffProfileQuery): MutableList<TieredTariffProfile> {
    return query.apply(store.list())
}
