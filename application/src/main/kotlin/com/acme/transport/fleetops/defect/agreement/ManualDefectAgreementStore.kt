package com.acme.transport.fleetops.defect.agreement

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps manual defect agreements by code. */
interface ManualDefectAgreementStore {
    fun find(code: String): ManualDefectAgreement?
    fun save(item: ManualDefectAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<ManualDefectAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryManualDefectAgreementStore(
    var items: MutableMap<String, ManualDefectAgreement>,
    var order: MutableList<String>,
) : ManualDefectAgreementStore {
    override fun find(code: String): ManualDefectAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ManualDefectAgreement) {
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

    override fun list(): MutableList<ManualDefectAgreement> {
        val out = mutableListOf<ManualDefectAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryManualDefectAgreementStore(): ManualDefectAgreementStore {
    return MemoryManualDefectAgreementStore(
        mutableMapOf<String, ManualDefectAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedManualDefectAgreementStore(
    var inner: ManualDefectAgreementStore,
    var cache: TtlCache<ManualDefectAgreement>,
    var hits: Int,
    var misses: Int,
) : ManualDefectAgreementStore {
    override fun find(code: String): ManualDefectAgreement? {
        val hit = (cache.get(code) as ManualDefectAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ManualDefectAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ManualDefectAgreement> = inner.list()
}

fun newCachedManualDefectAgreementStore(inner: ManualDefectAgreementStore, ttl: Int, clock: Clock): ManualDefectAgreementStore {
    return CachedManualDefectAgreementStore(
        inner = inner,
        cache = newTtlCache<ManualDefectAgreement>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects manual defect agreements. Empty fields match all. */
data class ManualDefectAgreementQuery(
    var locale: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ManualDefectAgreement): Boolean {
        if (locale != "" && normalize(item.locale) != normalize(locale)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ManualDefectAgreement>): MutableList<ManualDefectAgreement> {
        val matched = filter(items) { candidate: ManualDefectAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findManualDefectAgreements(store: ManualDefectAgreementStore, query: ManualDefectAgreementQuery): MutableList<ManualDefectAgreement> {
    return query.apply(store.list())
}
