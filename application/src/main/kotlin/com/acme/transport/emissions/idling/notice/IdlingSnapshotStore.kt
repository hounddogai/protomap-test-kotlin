package com.acme.transport.emissions.idling.notice

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling snapshots by code. */
interface IdlingSnapshotStore {
    fun find(code: String): IdlingSnapshot?
    fun save(item: IdlingSnapshot)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingSnapshot>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingSnapshotStore(
    var items: MutableMap<String, IdlingSnapshot>,
    var order: MutableList<String>,
) : IdlingSnapshotStore {
    override fun find(code: String): IdlingSnapshot? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingSnapshot) {
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

    override fun list(): MutableList<IdlingSnapshot> {
        val out = mutableListOf<IdlingSnapshot>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingSnapshotStore(): IdlingSnapshotStore {
    return MemoryIdlingSnapshotStore(mutableMapOf<String, IdlingSnapshot>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingSnapshotStore(
    var inner: IdlingSnapshotStore,
    var cache: TtlCache<IdlingSnapshot>,
    var hits: Int,
    var misses: Int,
) : IdlingSnapshotStore {
    override fun find(code: String): IdlingSnapshot? {
        val hit = (cache.get(code) as IdlingSnapshot?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingSnapshot) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingSnapshot> = inner.list()
}

fun newCachedIdlingSnapshotStore(inner: IdlingSnapshotStore, ttl: Int, clock: Clock): IdlingSnapshotStore {
    return CachedIdlingSnapshotStore(
        inner = inner,
        cache = newTtlCache<IdlingSnapshot>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling snapshots. Empty fields match all. */
data class IdlingSnapshotQuery(
    var segment: String,
    var minMinimum: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingSnapshot): Boolean {
        if (segment != "" && normalize(item.segment) != normalize(segment)) {
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

    fun apply(items: MutableList<IdlingSnapshot>): MutableList<IdlingSnapshot> {
        val matched = filter(items) { candidate: IdlingSnapshot -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingSnapshots(store: IdlingSnapshotStore, query: IdlingSnapshotQuery): MutableList<IdlingSnapshot> {
    return query.apply(store.list())
}
