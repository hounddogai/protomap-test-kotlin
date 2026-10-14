package com.acme.transport.safety.cameraevent.defaulteventscore

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps adaptive camera event groups by code. */
interface AdaptiveCameraEventGroupStore {
    fun find(code: String): AdaptiveCameraEventGroup?
    fun save(item: AdaptiveCameraEventGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<AdaptiveCameraEventGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAdaptiveCameraEventGroupStore(
    var items: MutableMap<String, AdaptiveCameraEventGroup>,
    var order: MutableList<String>,
) : AdaptiveCameraEventGroupStore {
    override fun find(code: String): AdaptiveCameraEventGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AdaptiveCameraEventGroup) {
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

    override fun list(): MutableList<AdaptiveCameraEventGroup> {
        val out = mutableListOf<AdaptiveCameraEventGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAdaptiveCameraEventGroupStore(): AdaptiveCameraEventGroupStore {
    return MemoryAdaptiveCameraEventGroupStore(
        mutableMapOf<String, AdaptiveCameraEventGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAdaptiveCameraEventGroupStore(
    var inner: AdaptiveCameraEventGroupStore,
    var cache: TtlCache<AdaptiveCameraEventGroup>,
    var hits: Int,
    var misses: Int,
) : AdaptiveCameraEventGroupStore {
    override fun find(code: String): AdaptiveCameraEventGroup? {
        val hit = (cache.get(code) as AdaptiveCameraEventGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AdaptiveCameraEventGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AdaptiveCameraEventGroup> = inner.list()
}

fun newCachedAdaptiveCameraEventGroupStore(inner: AdaptiveCameraEventGroupStore, ttl: Int, clock: Clock): AdaptiveCameraEventGroupStore {
    return CachedAdaptiveCameraEventGroupStore(
        inner = inner,
        cache = newTtlCache<AdaptiveCameraEventGroup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects adaptive camera event groups. Empty fields match all. */
data class AdaptiveCameraEventGroupQuery(
    var title: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AdaptiveCameraEventGroup): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
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

    fun apply(items: MutableList<AdaptiveCameraEventGroup>): MutableList<AdaptiveCameraEventGroup> {
        val matched = filter(items) { candidate: AdaptiveCameraEventGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAdaptiveCameraEventGroups(store: AdaptiveCameraEventGroupStore, query: AdaptiveCameraEventGroupQuery): MutableList<AdaptiveCameraEventGroup> {
    return query.apply(store.list())
}
