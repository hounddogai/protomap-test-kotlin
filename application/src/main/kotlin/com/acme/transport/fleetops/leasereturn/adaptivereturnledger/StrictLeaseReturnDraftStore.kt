package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import com.acme.transport.fleetops.cache.TtlCache
import com.acme.transport.fleetops.cache.newTtlCache
import com.acme.transport.fleetops.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict lease return drafts by code. */
interface StrictLeaseReturnDraftStore {
    fun find(code: String): StrictLeaseReturnDraft?
    fun save(item: StrictLeaseReturnDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictLeaseReturnDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictLeaseReturnDraftStore(
    var items: MutableMap<String, StrictLeaseReturnDraft>,
    var order: MutableList<String>,
) : StrictLeaseReturnDraftStore {
    override fun find(code: String): StrictLeaseReturnDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictLeaseReturnDraft) {
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

    override fun list(): MutableList<StrictLeaseReturnDraft> {
        val out = mutableListOf<StrictLeaseReturnDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictLeaseReturnDraftStore(): StrictLeaseReturnDraftStore {
    return MemoryStrictLeaseReturnDraftStore(
        mutableMapOf<String, StrictLeaseReturnDraft>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictLeaseReturnDraftStore(
    var inner: StrictLeaseReturnDraftStore,
    var cache: TtlCache<StrictLeaseReturnDraft>,
    var hits: Int,
    var misses: Int,
) : StrictLeaseReturnDraftStore {
    override fun find(code: String): StrictLeaseReturnDraft? {
        val hit = (cache.get(code) as StrictLeaseReturnDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictLeaseReturnDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictLeaseReturnDraft> = inner.list()
}

fun newCachedStrictLeaseReturnDraftStore(inner: StrictLeaseReturnDraftStore, ttl: Int, clock: Clock): StrictLeaseReturnDraftStore {
    return CachedStrictLeaseReturnDraftStore(
        inner = inner,
        cache = newTtlCache<StrictLeaseReturnDraft>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict lease return drafts. Empty fields match all. */
data class StrictLeaseReturnDraftQuery(
    var label: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictLeaseReturnDraft): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictLeaseReturnDraft>): MutableList<StrictLeaseReturnDraft> {
        val matched = filter(items) { candidate: StrictLeaseReturnDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictLeaseReturnDrafts(store: StrictLeaseReturnDraftStore, query: StrictLeaseReturnDraftQuery): MutableList<StrictLeaseReturnDraft> {
    return query.apply(store.list())
}
