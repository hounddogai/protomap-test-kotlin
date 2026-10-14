package com.acme.transport.network.zone.agreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps zone agreements by code. */
interface ZoneAgreementStore {
    fun find(code: String): ZoneAgreement?
    fun save(item: ZoneAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<ZoneAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryZoneAgreementStore(
    var items: MutableMap<String, ZoneAgreement>,
    var order: MutableList<String>,
) : ZoneAgreementStore {
    override fun find(code: String): ZoneAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ZoneAgreement) {
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

    override fun list(): MutableList<ZoneAgreement> {
        val out = mutableListOf<ZoneAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryZoneAgreementStore(): ZoneAgreementStore {
    return MemoryZoneAgreementStore(mutableMapOf<String, ZoneAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedZoneAgreementStore(
    var inner: ZoneAgreementStore,
    var cache: TtlCache<ZoneAgreement>,
    var hits: Int,
    var misses: Int,
) : ZoneAgreementStore {
    override fun find(code: String): ZoneAgreement? {
        val hit = (cache.get(code) as ZoneAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ZoneAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ZoneAgreement> = inner.list()
}

fun newCachedZoneAgreementStore(inner: ZoneAgreementStore, ttl: Int, clock: Clock): ZoneAgreementStore {
    return CachedZoneAgreementStore(
        inner = inner,
        cache = newTtlCache<ZoneAgreement>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects zone agreements. Empty fields match all. */
data class ZoneAgreementQuery(
    var locale: String,
    var minFloorValue: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ZoneAgreement): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.floorValue < minFloorValue) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ZoneAgreement>): MutableList<ZoneAgreement> {
        val matched = filter(items) { candidate: ZoneAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findZoneAgreements(store: ZoneAgreementStore, query: ZoneAgreementQuery): MutableList<ZoneAgreement> {
    return query.apply(store.list())
}
