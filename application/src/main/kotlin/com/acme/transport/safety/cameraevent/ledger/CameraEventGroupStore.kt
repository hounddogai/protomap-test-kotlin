package com.acme.transport.safety.cameraevent.ledger

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps camera event groups by code. */
interface CameraEventGroupStore {
    fun find(code: String): CameraEventGroup?
    fun save(item: CameraEventGroup)
    fun remove(code: String): Boolean
    fun list(): MutableList<CameraEventGroup>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryCameraEventGroupStore(
    var items: MutableMap<String, CameraEventGroup>,
    var order: MutableList<String>,
) : CameraEventGroupStore {
    override fun find(code: String): CameraEventGroup? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: CameraEventGroup) {
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

    override fun list(): MutableList<CameraEventGroup> {
        val out = mutableListOf<CameraEventGroup>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryCameraEventGroupStore(): CameraEventGroupStore {
    return MemoryCameraEventGroupStore(mutableMapOf<String, CameraEventGroup>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedCameraEventGroupStore(
    var inner: CameraEventGroupStore,
    var cache: TtlCache<CameraEventGroup>,
    var hits: Int,
    var misses: Int,
) : CameraEventGroupStore {
    override fun find(code: String): CameraEventGroup? {
        val hit = (cache.get(code) as CameraEventGroup?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: CameraEventGroup) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<CameraEventGroup> = inner.list()
}

fun newCachedCameraEventGroupStore(inner: CameraEventGroupStore, ttl: Int, clock: Clock): CameraEventGroupStore {
    return CachedCameraEventGroupStore(
        inner = inner,
        cache = newTtlCache<CameraEventGroup>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects camera event groups. Empty fields match all. */
data class CameraEventGroupQuery(
    var channel: String,
    var minRatio: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: CameraEventGroup): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
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

    fun apply(items: MutableList<CameraEventGroup>): MutableList<CameraEventGroup> {
        val matched = filter(items) { candidate: CameraEventGroup -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findCameraEventGroups(store: CameraEventGroupStore, query: CameraEventGroupQuery): MutableList<CameraEventGroup> {
    return query.apply(store.list())
}
