package com.acme.transport.network.servicearea.areaclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.network.cache.TtlCache
import com.acme.transport.network.cache.newTtlCache
import com.acme.transport.network.clock.Clock

/** Keeps default service area snapshots by code. */
interface DefaultServiceAreaSnapshotStore {
    fun find(code: String): DefaultServiceAreaSnapshot?
    fun save(item: DefaultServiceAreaSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultServiceAreaSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultServiceAreaSnapshotStore(
    var items: MutableMap<String, DefaultServiceAreaSnapshot>,
    var order: MutableList<String>,
) : DefaultServiceAreaSnapshotStore {
    override fun find(code: String): DefaultServiceAreaSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultServiceAreaSnapshot) {
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

    override fun list(): MutableList<DefaultServiceAreaSnapshot> {
        val out = mutableListOf<DefaultServiceAreaSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultServiceAreaSnapshotStore(): DefaultServiceAreaSnapshotStore {
    return MemoryDefaultServiceAreaSnapshotStore(
        mutableMapOf<String, DefaultServiceAreaSnapshot>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultServiceAreaSnapshotStore(
    var inner: DefaultServiceAreaSnapshotStore,
    var cache: TtlCache<DefaultServiceAreaSnapshot>,
    var hits: Int,
    var misses: Int,
) : DefaultServiceAreaSnapshotStore {
    override fun find(code: String): DefaultServiceAreaSnapshot? {
        val hit = (cache.get(code) as DefaultServiceAreaSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultServiceAreaSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultServiceAreaSnapshot> = inner.list()
}

fun newCachedDefaultServiceAreaSnapshotStore(inner: DefaultServiceAreaSnapshotStore, ttl: Int, clock: Clock): DefaultServiceAreaSnapshotStore {
    return CachedDefaultServiceAreaSnapshotStore(
        inner = inner,
        cache = newTtlCache<DefaultServiceAreaSnapshot>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default service area snapshots. Empty fields match all. */
data class DefaultServiceAreaSnapshotQuery(
    var name: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultServiceAreaSnapshot): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DefaultServiceAreaSnapshot>): MutableList<DefaultServiceAreaSnapshot> {
        val matched = filter(items) { candidate: DefaultServiceAreaSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultServiceAreaSnapshots(store: DefaultServiceAreaSnapshotStore, query: DefaultServiceAreaSnapshotQuery): MutableList<DefaultServiceAreaSnapshot> {
    return query.apply(store.list())
}
