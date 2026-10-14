package com.acme.transport.lastmile.parcel.exception

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps parcel agreements by code. */
interface ParcelAgreementStore {
    fun find(code: String): ParcelAgreement?
    fun save(item: ParcelAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<ParcelAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryParcelAgreementStore(
    var items: MutableMap<String, ParcelAgreement>,
    var order: MutableList<String>,
) : ParcelAgreementStore {
    override fun find(code: String): ParcelAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ParcelAgreement) {
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

    override fun list(): MutableList<ParcelAgreement> {
        val out = mutableListOf<ParcelAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryParcelAgreementStore(): ParcelAgreementStore {
    return MemoryParcelAgreementStore(mutableMapOf<String, ParcelAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedParcelAgreementStore(
    var inner: ParcelAgreementStore,
    var cache: TtlCache<ParcelAgreement>,
    var hits: Int,
    var misses: Int,
) : ParcelAgreementStore {
    override fun find(code: String): ParcelAgreement? {
        val hit = (cache.get(code) as ParcelAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ParcelAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ParcelAgreement> = inner.list()
}

fun newCachedParcelAgreementStore(inner: ParcelAgreementStore, ttl: Int, clock: Clock): ParcelAgreementStore {
    return CachedParcelAgreementStore(
        inner = inner,
        cache = newTtlCache<ParcelAgreement>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects parcel agreements. Empty fields match all. */
data class ParcelAgreementQuery(
    var source: String,
    var minMargin: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ParcelAgreement): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.margin < minMargin) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ParcelAgreement>): MutableList<ParcelAgreement> {
        val matched = filter(items) { candidate: ParcelAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findParcelAgreements(store: ParcelAgreementStore, query: ParcelAgreementQuery): MutableList<ParcelAgreement> {
    return query.apply(store.list())
}
