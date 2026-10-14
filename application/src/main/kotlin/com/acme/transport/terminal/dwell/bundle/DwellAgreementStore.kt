package com.acme.transport.terminal.dwell.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell agreements by code. */
interface DwellAgreementStore {
    fun find(code: String): DwellAgreement?
    fun save(item: DwellAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellAgreementStore(
    var items: MutableMap<String, DwellAgreement>,
    var order: MutableList<String>,
) : DwellAgreementStore {
    override fun find(code: String): DwellAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellAgreement) {
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

    override fun list(): MutableList<DwellAgreement> {
        val out = mutableListOf<DwellAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellAgreementStore(): DwellAgreementStore {
    return MemoryDwellAgreementStore(mutableMapOf<String, DwellAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellAgreementStore(
    var inner: DwellAgreementStore,
    var cache: TtlCache<DwellAgreement>,
    var hits: Int,
    var misses: Int,
) : DwellAgreementStore {
    override fun find(code: String): DwellAgreement? {
        val hit = (cache.get(code) as DwellAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellAgreement> = inner.list()
}

fun newCachedDwellAgreementStore(inner: DwellAgreementStore, ttl: Int, clock: Clock): DwellAgreementStore {
    return CachedDwellAgreementStore(
        inner = inner,
        cache = newTtlCache<DwellAgreement>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell agreements. Empty fields match all. */
data class DwellAgreementQuery(
    var lane: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellAgreement): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellAgreement>): MutableList<DwellAgreement> {
        val matched = filter(items) { candidate: DwellAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellAgreements(store: DwellAgreementStore, query: DwellAgreementQuery): MutableList<DwellAgreement> {
    return query.apply(store.list())
}
