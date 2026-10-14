package com.acme.transport.lastmile.locker.lockerclaim

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps primary locker drafts by code. */
interface PrimaryLockerDraftStore {
    fun find(code: String): PrimaryLockerDraft?
    fun save(item: PrimaryLockerDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<PrimaryLockerDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPrimaryLockerDraftStore(
    var items: MutableMap<String, PrimaryLockerDraft>,
    var order: MutableList<String>,
) : PrimaryLockerDraftStore {
    override fun find(code: String): PrimaryLockerDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PrimaryLockerDraft) {
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

    override fun list(): MutableList<PrimaryLockerDraft> {
        val out = mutableListOf<PrimaryLockerDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPrimaryLockerDraftStore(): PrimaryLockerDraftStore {
    return MemoryPrimaryLockerDraftStore(mutableMapOf<String, PrimaryLockerDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPrimaryLockerDraftStore(
    var inner: PrimaryLockerDraftStore,
    var cache: TtlCache<PrimaryLockerDraft>,
    var hits: Int,
    var misses: Int,
) : PrimaryLockerDraftStore {
    override fun find(code: String): PrimaryLockerDraft? {
        val hit = (cache.get(code) as PrimaryLockerDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PrimaryLockerDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PrimaryLockerDraft> = inner.list()
}

fun newCachedPrimaryLockerDraftStore(inner: PrimaryLockerDraftStore, ttl: Int, clock: Clock): PrimaryLockerDraftStore {
    return CachedPrimaryLockerDraftStore(
        inner = inner,
        cache = newTtlCache<PrimaryLockerDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects primary locker drafts. Empty fields match all. */
data class PrimaryLockerDraftQuery(
    var source: String,
    var minBatchSize: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PrimaryLockerDraft): Boolean {
        if (source != "" && normalize(item.source) != normalize(source)) {
            return false
        }
        if (item.batchSize < minBatchSize) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PrimaryLockerDraft>): MutableList<PrimaryLockerDraft> {
        val matched = filter(items) { candidate: PrimaryLockerDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPrimaryLockerDrafts(store: PrimaryLockerDraftStore, query: PrimaryLockerDraftQuery): MutableList<PrimaryLockerDraft> {
    return query.apply(store.list())
}
