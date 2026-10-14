package com.acme.transport.terminal.dockdoor.target

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps scheduled dock door drafts by code. */
interface ScheduledDockDoorDraftStore {
    fun find(code: String): ScheduledDockDoorDraft?
    fun save(item: ScheduledDockDoorDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<ScheduledDockDoorDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryScheduledDockDoorDraftStore(
    var items: MutableMap<String, ScheduledDockDoorDraft>,
    var order: MutableList<String>,
) : ScheduledDockDoorDraftStore {
    override fun find(code: String): ScheduledDockDoorDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: ScheduledDockDoorDraft) {
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

    override fun list(): MutableList<ScheduledDockDoorDraft> {
        val out = mutableListOf<ScheduledDockDoorDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryScheduledDockDoorDraftStore(): ScheduledDockDoorDraftStore {
    return MemoryScheduledDockDoorDraftStore(
        mutableMapOf<String, ScheduledDockDoorDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedScheduledDockDoorDraftStore(
    var inner: ScheduledDockDoorDraftStore,
    var cache: TtlCache<ScheduledDockDoorDraft>,
    var hits: Int,
    var misses: Int,
) : ScheduledDockDoorDraftStore {
    override fun find(code: String): ScheduledDockDoorDraft? {
        val hit = (cache.get(code) as ScheduledDockDoorDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: ScheduledDockDoorDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<ScheduledDockDoorDraft> = inner.list()
}

fun newCachedScheduledDockDoorDraftStore(inner: ScheduledDockDoorDraftStore, ttl: Int, clock: Clock): ScheduledDockDoorDraftStore {
    return CachedScheduledDockDoorDraftStore(
        inner = inner,
        cache = newTtlCache<ScheduledDockDoorDraft>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects scheduled dock door drafts. Empty fields match all. */
data class ScheduledDockDoorDraftQuery(
    var title: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: ScheduledDockDoorDraft): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.odometer < minOdometer) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<ScheduledDockDoorDraft>): MutableList<ScheduledDockDoorDraft> {
        val matched = filter(items) { candidate: ScheduledDockDoorDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findScheduledDockDoorDrafts(store: ScheduledDockDoorDraftStore, query: ScheduledDockDoorDraftQuery): MutableList<ScheduledDockDoorDraft> {
    return query.apply(store.list())
}
