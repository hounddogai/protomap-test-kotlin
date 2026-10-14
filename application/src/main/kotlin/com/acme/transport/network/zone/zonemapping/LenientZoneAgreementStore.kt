package com.acme.transport.network.zone.zonemapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps lenient zone agreements by code. */
interface LenientZoneAgreementStore {
    fun find(code: String): LenientZoneAgreement?
    fun save(item: LenientZoneAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientZoneAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientZoneAgreementStore(
    var items: MutableMap<String, LenientZoneAgreement>,
    var order: MutableList<String>,
) : LenientZoneAgreementStore {
    override fun find(code: String): LenientZoneAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientZoneAgreement) {
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

    override fun list(): MutableList<LenientZoneAgreement> {
        val out = mutableListOf<LenientZoneAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientZoneAgreementStore(): LenientZoneAgreementStore {
    return MemoryLenientZoneAgreementStore(
        mutableMapOf<String, LenientZoneAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientZoneAgreementStore(
    var inner: LenientZoneAgreementStore,
    var cache: TtlCache<LenientZoneAgreement>,
    var hits: Int,
    var misses: Int,
) : LenientZoneAgreementStore {
    override fun find(code: String): LenientZoneAgreement? {
        val hit = (cache.get(code) as LenientZoneAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientZoneAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientZoneAgreement> = inner.list()
}

fun newCachedLenientZoneAgreementStore(inner: LenientZoneAgreementStore, ttl: Int, clock: Clock): LenientZoneAgreementStore {
    return CachedLenientZoneAgreementStore(
        inner = inner,
        cache = newTtlCache<LenientZoneAgreement>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient zone agreements. Empty fields match all. */
data class LenientZoneAgreementQuery(
    var source: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientZoneAgreement): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.ratio < minRatio) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientZoneAgreement>): MutableList<LenientZoneAgreement> {
        val matched = filter(items) { candidate: LenientZoneAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientZoneAgreements(store: LenientZoneAgreementStore, query: LenientZoneAgreementQuery): MutableList<LenientZoneAgreement> {
    return query.apply(store.list())
}
