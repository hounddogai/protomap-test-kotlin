package com.acme.transport.terminal.dockdoor.checkpoint

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps legacy dock door drafts by code. */
interface LegacyDockDoorDraftStore {
    fun find(code: String): LegacyDockDoorDraft?
    fun save(item: LegacyDockDoorDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<LegacyDockDoorDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLegacyDockDoorDraftStore(
    var items: MutableMap<String, LegacyDockDoorDraft>,
    var order: MutableList<String>,
) : LegacyDockDoorDraftStore {
    override fun find(code: String): LegacyDockDoorDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LegacyDockDoorDraft) {
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

    override fun list(): MutableList<LegacyDockDoorDraft> {
        val out = mutableListOf<LegacyDockDoorDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLegacyDockDoorDraftStore(): LegacyDockDoorDraftStore {
    return MemoryLegacyDockDoorDraftStore(mutableMapOf<String, LegacyDockDoorDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLegacyDockDoorDraftStore(
    var inner: LegacyDockDoorDraftStore,
    var cache: TtlCache<LegacyDockDoorDraft>,
    var hits: Int,
    var misses: Int,
) : LegacyDockDoorDraftStore {
    override fun find(code: String): LegacyDockDoorDraft? {
        val hit = (cache.get(code) as LegacyDockDoorDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LegacyDockDoorDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LegacyDockDoorDraft> = inner.list()
}

fun newCachedLegacyDockDoorDraftStore(inner: LegacyDockDoorDraftStore, ttl: Int, clock: Clock): LegacyDockDoorDraftStore {
    return CachedLegacyDockDoorDraftStore(
        inner = inner,
        cache = newTtlCache<LegacyDockDoorDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects legacy dock door drafts. Empty fields match all. */
data class LegacyDockDoorDraftQuery(
    var title: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LegacyDockDoorDraft): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LegacyDockDoorDraft>): MutableList<LegacyDockDoorDraft> {
        val matched = filter(items) { candidate: LegacyDockDoorDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLegacyDockDoorDrafts(store: LegacyDockDoorDraftStore, query: LegacyDockDoorDraftQuery): MutableList<LegacyDockDoorDraft> {
    return query.apply(store.list())
}
