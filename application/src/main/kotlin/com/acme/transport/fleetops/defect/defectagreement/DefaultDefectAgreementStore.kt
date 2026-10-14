package com.acme.transport.fleetops.defect.defectagreement

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default defect agreements by code. */
interface DefaultDefectAgreementStore {
    fun find(code: String): DefaultDefectAgreement?
    fun save(item: DefaultDefectAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultDefectAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultDefectAgreementStore(
    var items: MutableMap<String, DefaultDefectAgreement>,
    var order: MutableList<String>,
) : DefaultDefectAgreementStore {
    override fun find(code: String): DefaultDefectAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultDefectAgreement) {
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

    override fun list(): MutableList<DefaultDefectAgreement> {
        val out = mutableListOf<DefaultDefectAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultDefectAgreementStore(): DefaultDefectAgreementStore {
    return MemoryDefaultDefectAgreementStore(
        mutableMapOf<String, DefaultDefectAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultDefectAgreementStore(
    var inner: DefaultDefectAgreementStore,
    var cache: TtlCache<DefaultDefectAgreement>,
    var hits: Int,
    var misses: Int,
) : DefaultDefectAgreementStore {
    override fun find(code: String): DefaultDefectAgreement? {
        val hit = (cache.get(code) as DefaultDefectAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultDefectAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultDefectAgreement> = inner.list()
}

fun newCachedDefaultDefectAgreementStore(inner: DefaultDefectAgreementStore, ttl: Int, clock: Clock): DefaultDefectAgreementStore {
    return CachedDefaultDefectAgreementStore(
        inner = inner,
        cache = newTtlCache<DefaultDefectAgreement>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default defect agreements. Empty fields match all. */
data class DefaultDefectAgreementQuery(
    var lane: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultDefectAgreement): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultDefectAgreement>): MutableList<DefaultDefectAgreement> {
        val matched = filter(items) { candidate: DefaultDefectAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultDefectAgreements(store: DefaultDefectAgreementStore, query: DefaultDefectAgreementQuery): MutableList<DefaultDefectAgreement> {
    return query.apply(store.list())
}
