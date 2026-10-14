package com.acme.transport.emissions.emissionfactor.draft

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps emission factor drafts by code. */
interface EmissionFactorDraftStore {
    fun find(code: String): EmissionFactorDraft?
    fun save(item: EmissionFactorDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<EmissionFactorDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryEmissionFactorDraftStore(
    var items: MutableMap<String, EmissionFactorDraft>,
    var order: MutableList<String>,
) : EmissionFactorDraftStore {
    override fun find(code: String): EmissionFactorDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: EmissionFactorDraft) {
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

    override fun list(): MutableList<EmissionFactorDraft> {
        val out = mutableListOf<EmissionFactorDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryEmissionFactorDraftStore(): EmissionFactorDraftStore {
    return MemoryEmissionFactorDraftStore(mutableMapOf<String, EmissionFactorDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedEmissionFactorDraftStore(
    var inner: EmissionFactorDraftStore,
    var cache: TtlCache<EmissionFactorDraft>,
    var hits: Int,
    var misses: Int,
) : EmissionFactorDraftStore {
    override fun find(code: String): EmissionFactorDraft? {
        val hit = (cache.get(code) as EmissionFactorDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: EmissionFactorDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<EmissionFactorDraft> = inner.list()
}

fun newCachedEmissionFactorDraftStore(inner: EmissionFactorDraftStore, ttl: Int, clock: Clock): EmissionFactorDraftStore {
    return CachedEmissionFactorDraftStore(
        inner = inner,
        cache = newTtlCache<EmissionFactorDraft>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects emission factor drafts. Empty fields match all. */
data class EmissionFactorDraftQuery(
    var unit: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: EmissionFactorDraft): Boolean {
        if (unit != "" && normalize(item.unit) != normalize(unit)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<EmissionFactorDraft>): MutableList<EmissionFactorDraft> {
        val matched = filter(items) { candidate: EmissionFactorDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findEmissionFactorDrafts(store: EmissionFactorDraftStore, query: EmissionFactorDraftQuery): MutableList<EmissionFactorDraft> {
    return query.apply(store.list())
}
