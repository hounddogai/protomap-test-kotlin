package com.acme.transport.crewing.roster.rosterquota

import com.acme.transport.crewing.cache.TtlCache
import com.acme.transport.crewing.cache.newTtlCache
import com.acme.transport.crewing.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps roster checkpoints by code. */
interface RosterCheckpointStore {
    fun find(code: String): RosterCheckpoint?
    fun save(item: RosterCheckpoint)
    fun remove(code: String): Boolean
    fun list(): MutableList<RosterCheckpoint>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRosterCheckpointStore(
    var items: MutableMap<String, RosterCheckpoint>,
    var order: MutableList<String>,
) : RosterCheckpointStore {
    override fun find(code: String): RosterCheckpoint? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RosterCheckpoint) {
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

    override fun list(): MutableList<RosterCheckpoint> {
        val out = mutableListOf<RosterCheckpoint>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRosterCheckpointStore(): RosterCheckpointStore {
    return MemoryRosterCheckpointStore(mutableMapOf<String, RosterCheckpoint>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRosterCheckpointStore(
    var inner: RosterCheckpointStore,
    var cache: TtlCache<RosterCheckpoint>,
    var hits: Int,
    var misses: Int,
) : RosterCheckpointStore {
    override fun find(code: String): RosterCheckpoint? {
        val hit = (cache.get(code) as RosterCheckpoint?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RosterCheckpoint) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RosterCheckpoint> = inner.list()
}

fun newCachedRosterCheckpointStore(inner: RosterCheckpointStore, ttl: Int, clock: Clock): RosterCheckpointStore {
    return CachedRosterCheckpointStore(
        inner = inner,
        cache = newTtlCache<RosterCheckpoint>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects roster checkpoints. Empty fields match all. */
data class RosterCheckpointQuery(
    var name: String,
    var minCapacity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RosterCheckpoint): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.capacity < minCapacity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RosterCheckpoint>): MutableList<RosterCheckpoint> {
        val matched = filter(items) { candidate: RosterCheckpoint -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRosterCheckpoints(store: RosterCheckpointStore, query: RosterCheckpointQuery): MutableList<RosterCheckpoint> {
    return query.apply(store.list())
}
