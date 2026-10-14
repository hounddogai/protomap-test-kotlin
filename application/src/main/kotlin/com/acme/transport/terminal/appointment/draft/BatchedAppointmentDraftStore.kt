package com.acme.transport.terminal.appointment.draft

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps batched appointment drafts by code. */
interface BatchedAppointmentDraftStore {
    fun find(code: String): BatchedAppointmentDraft?
    fun save(item: BatchedAppointmentDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<BatchedAppointmentDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBatchedAppointmentDraftStore(
    var items: MutableMap<String, BatchedAppointmentDraft>,
    var order: MutableList<String>,
) : BatchedAppointmentDraftStore {
    override fun find(code: String): BatchedAppointmentDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BatchedAppointmentDraft) {
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

    override fun list(): MutableList<BatchedAppointmentDraft> {
        val out = mutableListOf<BatchedAppointmentDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBatchedAppointmentDraftStore(): BatchedAppointmentDraftStore {
    return MemoryBatchedAppointmentDraftStore(
        mutableMapOf<String, BatchedAppointmentDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBatchedAppointmentDraftStore(
    var inner: BatchedAppointmentDraftStore,
    var cache: TtlCache<BatchedAppointmentDraft>,
    var hits: Int,
    var misses: Int,
) : BatchedAppointmentDraftStore {
    override fun find(code: String): BatchedAppointmentDraft? {
        val hit = (cache.get(code) as BatchedAppointmentDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BatchedAppointmentDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BatchedAppointmentDraft> = inner.list()
}

fun newCachedBatchedAppointmentDraftStore(inner: BatchedAppointmentDraftStore, ttl: Int, clock: Clock): BatchedAppointmentDraftStore {
    return CachedBatchedAppointmentDraftStore(
        inner = inner,
        cache = newTtlCache<BatchedAppointmentDraft>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects batched appointment drafts. Empty fields match all. */
data class BatchedAppointmentDraftQuery(
    var name: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BatchedAppointmentDraft): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<BatchedAppointmentDraft>): MutableList<BatchedAppointmentDraft> {
        val matched = filter(items) { candidate: BatchedAppointmentDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBatchedAppointmentDrafts(store: BatchedAppointmentDraftStore, query: BatchedAppointmentDraftQuery): MutableList<BatchedAppointmentDraft> {
    return query.apply(store.list())
}
