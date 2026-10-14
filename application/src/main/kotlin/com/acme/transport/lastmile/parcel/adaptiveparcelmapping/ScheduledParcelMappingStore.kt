package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps scheduled parcel mappings by code. */
interface ScheduledParcelMappingStore {
    fun find(code: String): ScheduledParcelMapping?
    fun save(item: ScheduledParcelMapping)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledParcelMapping>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledParcelMappingStore(
    var items: MutableMap<String, ScheduledParcelMapping>,
    var order: MutableList<String>,
) : ScheduledParcelMappingStore {
    override fun find(code: String): ScheduledParcelMapping? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledParcelMapping) {
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

    override fun list(): MutableList<ScheduledParcelMapping> {
        val out = mutableListOf<ScheduledParcelMapping>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledParcelMappingStore(): ScheduledParcelMappingStore {
    return MemoryScheduledParcelMappingStore(
        mutableMapOf<String, ScheduledParcelMapping>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledParcelMappingStore(
    var inner: ScheduledParcelMappingStore,
    var cache: TtlCache<ScheduledParcelMapping>,
    var hits: Int,
    var misses: Int,
) : ScheduledParcelMappingStore {
    override fun find(code: String): ScheduledParcelMapping? {
        val hit = (cache.get(code) as ScheduledParcelMapping?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledParcelMapping) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledParcelMapping> = inner.list()
}

fun newCachedScheduledParcelMappingStore(inner: ScheduledParcelMappingStore, ttl: Int, clock: Clock): ScheduledParcelMappingStore {
    return CachedScheduledParcelMappingStore(
        inner = inner,
        cache = newTtlCache<ScheduledParcelMapping>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled parcel mappings. Empty fields match all. */
data class ScheduledParcelMappingQuery(
    var title: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledParcelMapping): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledParcelMapping>): MutableList<ScheduledParcelMapping> {
        val matched = filter(items) { candidate: ScheduledParcelMapping -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledParcelMappings(store: ScheduledParcelMappingStore, query: ScheduledParcelMappingQuery): MutableList<ScheduledParcelMapping> {
    return query.apply(store.list())
}
