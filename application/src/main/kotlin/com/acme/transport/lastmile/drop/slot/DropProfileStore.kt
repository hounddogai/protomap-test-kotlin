package com.acme.transport.lastmile.drop.slot

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop profiles by code. */
interface DropProfileStore {
    fun find(code: String): DropProfile?
    fun save(item: DropProfile)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropProfile>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropProfileStore(
    var items: MutableMap<String, DropProfile>,
    var order: MutableList<String>,
) : DropProfileStore {
    override fun find(code: String): DropProfile? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropProfile) {
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

    override fun list(): MutableList<DropProfile> {
        val out = mutableListOf<DropProfile>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropProfileStore(): DropProfileStore {
    return MemoryDropProfileStore(mutableMapOf<String, DropProfile>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropProfileStore(
    var inner: DropProfileStore,
    var cache: TtlCache<DropProfile>,
    var hits: Int,
    var misses: Int,
) : DropProfileStore {
    override fun find(code: String): DropProfile? {
        val hit = (cache.get(code) as DropProfile?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropProfile) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropProfile> = inner.list()
}

fun newCachedDropProfileStore(inner: DropProfileStore, ttl: Int, clock: Clock): DropProfileStore {
    return CachedDropProfileStore(
        inner = inner,
        cache = newTtlCache<DropProfile>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop profiles. Empty fields match all. */
data class DropProfileQuery(
    var label: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropProfile): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<DropProfile>): MutableList<DropProfile> {
        val matched = filter(items) { candidate: DropProfile -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropProfiles(store: DropProfileStore, query: DropProfileQuery): MutableList<DropProfile> {
    return query.apply(store.list())
}
