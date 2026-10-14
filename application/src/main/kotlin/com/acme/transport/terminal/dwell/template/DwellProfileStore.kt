package com.acme.transport.terminal.dwell.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps dwell profiles by code. */
interface DwellProfileStore {
    fun find(code: String): DwellProfile?
    fun save(item: DwellProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<DwellProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDwellProfileStore(
    var items: MutableMap<String, DwellProfile>,
    var order: MutableList<String>,
) : DwellProfileStore {
    override fun find(code: String): DwellProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DwellProfile) {
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

    override fun list(): MutableList<DwellProfile> {
        val out = mutableListOf<DwellProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDwellProfileStore(): DwellProfileStore {
    return MemoryDwellProfileStore(mutableMapOf<String, DwellProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDwellProfileStore(
    var inner: DwellProfileStore,
    var cache: TtlCache<DwellProfile>,
    var hits: Int,
    var misses: Int,
) : DwellProfileStore {
    override fun find(code: String): DwellProfile? {
        val hit = (cache.get(code) as DwellProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DwellProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DwellProfile> = inner.list()
}

fun newCachedDwellProfileStore(inner: DwellProfileStore, ttl: Int, clock: Clock): DwellProfileStore {
    return CachedDwellProfileStore(
        inner = inner,
        cache = newTtlCache<DwellProfile>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dwell profiles. Empty fields match all. */
data class DwellProfileQuery(
    var title: String,
    var minLimit: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DwellProfile): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.limit < minLimit) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DwellProfile>): MutableList<DwellProfile> {
        val matched = filter(items) { candidate: DwellProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDwellProfiles(store: DwellProfileStore, query: DwellProfileQuery): MutableList<DwellProfile> {
    return query.apply(store.list())
}
