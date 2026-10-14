package com.acme.transport.safety.cameraevent.group

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps default camera event groups by code. */
interface DefaultCameraEventGroupStore {
    fun find(code: String): DefaultCameraEventGroup?
    fun save(item: DefaultCameraEventGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultCameraEventGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultCameraEventGroupStore(
    var items: MutableMap<String, DefaultCameraEventGroup>,
    var order: MutableList<String>,
) : DefaultCameraEventGroupStore {
    override fun find(code: String): DefaultCameraEventGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultCameraEventGroup) {
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

    override fun list(): MutableList<DefaultCameraEventGroup> {
        val out = mutableListOf<DefaultCameraEventGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultCameraEventGroupStore(): DefaultCameraEventGroupStore {
    return MemoryDefaultCameraEventGroupStore(
        mutableMapOf<String, DefaultCameraEventGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultCameraEventGroupStore(
    var inner: DefaultCameraEventGroupStore,
    var cache: TtlCache<DefaultCameraEventGroup>,
    var hits: Int,
    var misses: Int,
) : DefaultCameraEventGroupStore {
    override fun find(code: String): DefaultCameraEventGroup? {
        val hit = (cache.get(code) as DefaultCameraEventGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultCameraEventGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultCameraEventGroup> = inner.list()
}

fun newCachedDefaultCameraEventGroupStore(inner: DefaultCameraEventGroupStore, ttl: Int, clock: Clock): DefaultCameraEventGroupStore {
    return CachedDefaultCameraEventGroupStore(
        inner = inner,
        cache = newTtlCache<DefaultCameraEventGroup>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default camera event groups. Empty fields match all. */
data class DefaultCameraEventGroupQuery(
    var depot: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultCameraEventGroup): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
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

    fun apply(items: MutableList<DefaultCameraEventGroup>): MutableList<DefaultCameraEventGroup> {
        val matched = filter(items) { candidate: DefaultCameraEventGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultCameraEventGroups(store: DefaultCameraEventGroupStore, query: DefaultCameraEventGroupQuery): MutableList<DefaultCameraEventGroup> {
    return query.apply(store.list())
}
