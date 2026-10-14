package com.acme.transport.network.cutoff.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps cutoff groups by code. */
interface CutoffGroupStore {
    fun find(code: String): CutoffGroup?
    fun save(item: CutoffGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<CutoffGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCutoffGroupStore(
    var items: MutableMap<String, CutoffGroup>,
    var order: MutableList<String>,
) : CutoffGroupStore {
    override fun find(code: String): CutoffGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CutoffGroup) {
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

    override fun list(): MutableList<CutoffGroup> {
        val out = mutableListOf<CutoffGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCutoffGroupStore(): CutoffGroupStore {
    return MemoryCutoffGroupStore(mutableMapOf<String, CutoffGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCutoffGroupStore(
    var inner: CutoffGroupStore,
    var cache: TtlCache<CutoffGroup>,
    var hits: Int,
    var misses: Int,
) : CutoffGroupStore {
    override fun find(code: String): CutoffGroup? {
        val hit = (cache.get(code) as CutoffGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CutoffGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CutoffGroup> = inner.list()
}

fun newCachedCutoffGroupStore(inner: CutoffGroupStore, ttl: Int, clock: Clock): CutoffGroupStore {
    return CachedCutoffGroupStore(
        inner = inner,
        cache = newTtlCache<CutoffGroup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects cutoff groups. Empty fields match all. */
data class CutoffGroupQuery(
    var label: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CutoffGroup): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<CutoffGroup>): MutableList<CutoffGroup> {
        val matched = filter(items) { candidate: CutoffGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCutoffGroups(store: CutoffGroupStore, query: CutoffGroupQuery): MutableList<CutoffGroup> {
    return query.apply(store.list())
}
