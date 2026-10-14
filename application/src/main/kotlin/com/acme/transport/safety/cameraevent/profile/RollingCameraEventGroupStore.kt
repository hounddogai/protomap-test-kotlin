package com.acme.transport.safety.cameraevent.profile

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps rolling camera event groups by code. */
interface RollingCameraEventGroupStore {
    fun find(code: String): RollingCameraEventGroup?
    fun save(item: RollingCameraEventGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingCameraEventGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingCameraEventGroupStore(
    var items: MutableMap<String, RollingCameraEventGroup>,
    var order: MutableList<String>,
) : RollingCameraEventGroupStore {
    override fun find(code: String): RollingCameraEventGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingCameraEventGroup) {
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

    override fun list(): MutableList<RollingCameraEventGroup> {
        val out = mutableListOf<RollingCameraEventGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingCameraEventGroupStore(): RollingCameraEventGroupStore {
    return MemoryRollingCameraEventGroupStore(
        mutableMapOf<String, RollingCameraEventGroup>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingCameraEventGroupStore(
    var inner: RollingCameraEventGroupStore,
    var cache: TtlCache<RollingCameraEventGroup>,
    var hits: Int,
    var misses: Int,
) : RollingCameraEventGroupStore {
    override fun find(code: String): RollingCameraEventGroup? {
        val hit = (cache.get(code) as RollingCameraEventGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingCameraEventGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingCameraEventGroup> = inner.list()
}

fun newCachedRollingCameraEventGroupStore(inner: RollingCameraEventGroupStore, ttl: Int, clock: Clock): RollingCameraEventGroupStore {
    return CachedRollingCameraEventGroupStore(
        inner = inner,
        cache = newTtlCache<RollingCameraEventGroup>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling camera event groups. Empty fields match all. */
data class RollingCameraEventGroupQuery(
    var name: String,
    var minRetries: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingCameraEventGroup): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.retries < minRetries) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingCameraEventGroup>): MutableList<RollingCameraEventGroup> {
        val matched = filter(items) { candidate: RollingCameraEventGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingCameraEventGroups(store: RollingCameraEventGroupStore, query: RollingCameraEventGroupQuery): MutableList<RollingCameraEventGroup> {
    return query.apply(store.list())
}
