package com.acme.transport.emissions.fuelburn.estimate

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps fuel burn drafts by code. */
interface FuelBurnDraftStore {
    fun find(code: String): FuelBurnDraft?
    fun save(item: FuelBurnDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<FuelBurnDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryFuelBurnDraftStore(
    var items: MutableMap<String, FuelBurnDraft>,
    var order: MutableList<String>,
) : FuelBurnDraftStore {
    override fun find(code: String): FuelBurnDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: FuelBurnDraft) {
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

    override fun list(): MutableList<FuelBurnDraft> {
        val out = mutableListOf<FuelBurnDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryFuelBurnDraftStore(): FuelBurnDraftStore {
    return MemoryFuelBurnDraftStore(mutableMapOf<String, FuelBurnDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedFuelBurnDraftStore(
    var inner: FuelBurnDraftStore,
    var cache: TtlCache<FuelBurnDraft>,
    var hits: Int,
    var misses: Int,
) : FuelBurnDraftStore {
    override fun find(code: String): FuelBurnDraft? {
        val hit = (cache.get(code) as FuelBurnDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: FuelBurnDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<FuelBurnDraft> = inner.list()
}

fun newCachedFuelBurnDraftStore(inner: FuelBurnDraftStore, ttl: Int, clock: Clock): FuelBurnDraftStore {
    return CachedFuelBurnDraftStore(
        inner = inner,
        cache = newTtlCache<FuelBurnDraft>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects fuel burn drafts. Empty fields match all. */
data class FuelBurnDraftQuery(
    var name: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: FuelBurnDraft): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<FuelBurnDraft>): MutableList<FuelBurnDraft> {
        val matched = filter(items) { candidate: FuelBurnDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findFuelBurnDrafts(store: FuelBurnDraftStore, query: FuelBurnDraftQuery): MutableList<FuelBurnDraft> {
    return query.apply(store.list())
}
