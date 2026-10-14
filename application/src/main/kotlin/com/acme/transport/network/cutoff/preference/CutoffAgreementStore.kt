package com.acme.transport.network.cutoff.preference

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff agreements by code. */
interface CutoffAgreementStore {
    fun find(code: String): CutoffAgreement?
    fun save(item: CutoffAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffAgreementStore(
    var items: MutableMap<String, CutoffAgreement>,
    var order: MutableList<String>,
) : CutoffAgreementStore {
    override fun find(code: String): CutoffAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffAgreement) {
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

    override fun list(): MutableList<CutoffAgreement> {
        val out = mutableListOf<CutoffAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffAgreementStore(): CutoffAgreementStore {
    return MemoryCutoffAgreementStore(mutableMapOf<String, CutoffAgreement>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffAgreementStore(
    var inner: CutoffAgreementStore,
    var cache: TtlCache<CutoffAgreement>,
    var hits: Int,
    var misses: Int,
) : CutoffAgreementStore {
    override fun find(code: String): CutoffAgreement? {
        val hit = (cache.get(code) as CutoffAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffAgreement> = inner.list()
}

fun newCachedCutoffAgreementStore(inner: CutoffAgreementStore, ttl: Int, clock: Clock): CutoffAgreementStore {
    return CachedCutoffAgreementStore(
        inner = inner,
        cache = newTtlCache<CutoffAgreement>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff agreements. Empty fields match all. */
data class CutoffAgreementQuery(
    var region: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffAgreement): Boolean {
        if (region != "" && normalize(item.region) != normalize(region)) {
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

    fun apply(items: MutableList<CutoffAgreement>): MutableList<CutoffAgreement> {
        val matched = filter(items) { candidate: CutoffAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffAgreements(store: CutoffAgreementStore, query: CutoffAgreementQuery): MutableList<CutoffAgreement> {
    return query.apply(store.list())
}
