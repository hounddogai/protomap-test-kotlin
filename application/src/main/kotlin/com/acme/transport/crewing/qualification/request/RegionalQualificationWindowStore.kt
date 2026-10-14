package com.acme.transport.crewing.qualification.request

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional qualification windows by code. */
interface RegionalQualificationWindowStore {
    fun find(code: String): RegionalQualificationWindow?
    fun save(item: RegionalQualificationWindow)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalQualificationWindow>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalQualificationWindowStore(
    var items: MutableMap<String, RegionalQualificationWindow>,
    var order: MutableList<String>,
) : RegionalQualificationWindowStore {
    override fun find(code: String): RegionalQualificationWindow? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalQualificationWindow) {
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

    override fun list(): MutableList<RegionalQualificationWindow> {
        val out = mutableListOf<RegionalQualificationWindow>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalQualificationWindowStore(): RegionalQualificationWindowStore {
    return MemoryRegionalQualificationWindowStore(
        mutableMapOf<String, RegionalQualificationWindow>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalQualificationWindowStore(
    var inner: RegionalQualificationWindowStore,
    var cache: TtlCache<RegionalQualificationWindow>,
    var hits: Int,
    var misses: Int,
) : RegionalQualificationWindowStore {
    override fun find(code: String): RegionalQualificationWindow? {
        val hit = (cache.get(code) as RegionalQualificationWindow?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalQualificationWindow) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalQualificationWindow> = inner.list()
}

fun newCachedRegionalQualificationWindowStore(inner: RegionalQualificationWindowStore, ttl: Int, clock: Clock): RegionalQualificationWindowStore {
    return CachedRegionalQualificationWindowStore(
        inner = inner,
        cache = newTtlCache<RegionalQualificationWindow>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional qualification windows. Empty fields match all. */
data class RegionalQualificationWindowQuery(
    var name: String,
    var minEtaMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalQualificationWindow): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<RegionalQualificationWindow>): MutableList<RegionalQualificationWindow> {
        val matched = filter(items) { candidate: RegionalQualificationWindow -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalQualificationWindows(store: RegionalQualificationWindowStore, query: RegionalQualificationWindowQuery): MutableList<RegionalQualificationWindow> {
    return query.apply(store.list())
}
