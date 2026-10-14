package com.acme.transport.customs.broker.threshold

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps pending broker drafts by code. */
interface PendingBrokerDraftStore {
    fun find(code: String): PendingBrokerDraft?
    fun save(item: PendingBrokerDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<PendingBrokerDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryPendingBrokerDraftStore(
    var items: MutableMap<String, PendingBrokerDraft>,
    var order: MutableList<String>,
) : PendingBrokerDraftStore {
    override fun find(code: String): PendingBrokerDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: PendingBrokerDraft) {
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

    override fun list(): MutableList<PendingBrokerDraft> {
        val out = mutableListOf<PendingBrokerDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryPendingBrokerDraftStore(): PendingBrokerDraftStore {
    return MemoryPendingBrokerDraftStore(mutableMapOf<String, PendingBrokerDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedPendingBrokerDraftStore(
    var inner: PendingBrokerDraftStore,
    var cache: TtlCache<PendingBrokerDraft>,
    var hits: Int,
    var misses: Int,
) : PendingBrokerDraftStore {
    override fun find(code: String): PendingBrokerDraft? {
        val hit = (cache.get(code) as PendingBrokerDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: PendingBrokerDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<PendingBrokerDraft> = inner.list()
}

fun newCachedPendingBrokerDraftStore(inner: PendingBrokerDraftStore, ttl: Int, clock: Clock): PendingBrokerDraftStore {
    return CachedPendingBrokerDraftStore(
        inner = inner,
        cache = newTtlCache<PendingBrokerDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects pending broker drafts. Empty fields match all. */
data class PendingBrokerDraftQuery(
    var label: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: PendingBrokerDraft): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<PendingBrokerDraft>): MutableList<PendingBrokerDraft> {
        val matched = filter(items) { candidate: PendingBrokerDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findPendingBrokerDrafts(store: PendingBrokerDraftStore, query: PendingBrokerDraftQuery): MutableList<PendingBrokerDraft> {
    return query.apply(store.list())
}
