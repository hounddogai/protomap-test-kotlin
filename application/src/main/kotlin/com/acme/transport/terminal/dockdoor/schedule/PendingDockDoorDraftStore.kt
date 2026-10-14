package com.acme.transport.terminal.dockdoor.schedule

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps pending dock door drafts by code. */
interface PendingDockDoorDraftStore {
    fun find(code: String): PendingDockDoorDraft?
    fun save(item: PendingDockDoorDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingDockDoorDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingDockDoorDraftStore(
    var items: MutableMap<String, PendingDockDoorDraft>,
    var order: MutableList<String>,
) : PendingDockDoorDraftStore {
    override fun find(code: String): PendingDockDoorDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingDockDoorDraft) {
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

    override fun list(): MutableList<PendingDockDoorDraft> {
        val out = mutableListOf<PendingDockDoorDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingDockDoorDraftStore(): PendingDockDoorDraftStore {
    return MemoryPendingDockDoorDraftStore(
        mutableMapOf<String, PendingDockDoorDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingDockDoorDraftStore(
    var inner: PendingDockDoorDraftStore,
    var cache: TtlCache<PendingDockDoorDraft>,
    var hits: Int,
    var misses: Int,
) : PendingDockDoorDraftStore {
    override fun find(code: String): PendingDockDoorDraft? {
        val hit = (cache.get(code) as PendingDockDoorDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingDockDoorDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingDockDoorDraft> = inner.list()
}

fun newCachedPendingDockDoorDraftStore(inner: PendingDockDoorDraftStore, ttl: Int, clock: Clock): PendingDockDoorDraftStore {
    return CachedPendingDockDoorDraftStore(
        inner = inner,
        cache = newTtlCache<PendingDockDoorDraft>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending dock door drafts. Empty fields match all. */
data class PendingDockDoorDraftQuery(
    var lane: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingDockDoorDraft): Boolean {
        if (lane != "" && normalize(item.lane) != normalize(lane)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingDockDoorDraft>): MutableList<PendingDockDoorDraft> {
        val matched = filter(items) { candidate: PendingDockDoorDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingDockDoorDrafts(store: PendingDockDoorDraftStore, query: PendingDockDoorDraftQuery): MutableList<PendingDockDoorDraft> {
    return query.apply(store.list())
}
