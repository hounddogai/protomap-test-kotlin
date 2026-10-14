package com.acme.transport.terminal.dwell.bundle

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell overrides by code. */
interface DwellOverrideStore {
    fun find(code: String): DwellOverride?
    fun save(item: DwellOverride)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellOverride>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellOverrideStore(
    var items: MutableMap<String, DwellOverride>,
    var order: MutableList<String>,
) : DwellOverrideStore {
    override fun find(code: String): DwellOverride? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellOverride) {
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

    override fun list(): MutableList<DwellOverride> {
        val out = mutableListOf<DwellOverride>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellOverrideStore(): DwellOverrideStore {
    return MemoryDwellOverrideStore(mutableMapOf<String, DwellOverride>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellOverrideStore(
    var inner: DwellOverrideStore,
    var cache: TtlCache<DwellOverride>,
    var hits: Int,
    var misses: Int,
) : DwellOverrideStore {
    override fun find(code: String): DwellOverride? {
        val hit = (cache.get(code) as DwellOverride?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellOverride) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellOverride> = inner.list()
}

fun newCachedDwellOverrideStore(inner: DwellOverrideStore, ttl: Int, clock: Clock): DwellOverrideStore {
    return CachedDwellOverrideStore(
        inner = inner,
        cache = newTtlCache<DwellOverride>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell overrides. Empty fields match all. */
data class DwellOverrideQuery(
    var title: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellOverride): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<DwellOverride>): MutableList<DwellOverride> {
        val matched = filter(items) { candidate: DwellOverride -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellOverrides(store: DwellOverrideStore, query: DwellOverrideQuery): MutableList<DwellOverride> {
    return query.apply(store.list())
}
