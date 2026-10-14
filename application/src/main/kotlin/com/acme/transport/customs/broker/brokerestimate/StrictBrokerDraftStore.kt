package com.acme.transport.customs.broker.brokerestimate

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict broker drafts by code. */
interface StrictBrokerDraftStore {
    fun find(code: String): StrictBrokerDraft?
    fun save(item: StrictBrokerDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictBrokerDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictBrokerDraftStore(
    var items: MutableMap<String, StrictBrokerDraft>,
    var order: MutableList<String>,
) : StrictBrokerDraftStore {
    override fun find(code: String): StrictBrokerDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictBrokerDraft) {
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

    override fun list(): MutableList<StrictBrokerDraft> {
        val out = mutableListOf<StrictBrokerDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictBrokerDraftStore(): StrictBrokerDraftStore {
    return MemoryStrictBrokerDraftStore(mutableMapOf<String, StrictBrokerDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictBrokerDraftStore(
    var inner: StrictBrokerDraftStore,
    var cache: TtlCache<StrictBrokerDraft>,
    var hits: Int,
    var misses: Int,
) : StrictBrokerDraftStore {
    override fun find(code: String): StrictBrokerDraft? {
        val hit = (cache.get(code) as StrictBrokerDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictBrokerDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictBrokerDraft> = inner.list()
}

fun newCachedStrictBrokerDraftStore(inner: StrictBrokerDraftStore, ttl: Int, clock: Clock): StrictBrokerDraftStore {
    return CachedStrictBrokerDraftStore(
        inner = inner,
        cache = newTtlCache<StrictBrokerDraft>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict broker drafts. Empty fields match all. */
data class StrictBrokerDraftQuery(
    var channel: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictBrokerDraft): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictBrokerDraft>): MutableList<StrictBrokerDraft> {
        val matched = filter(items) { candidate: StrictBrokerDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictBrokerDrafts(store: StrictBrokerDraftStore, query: StrictBrokerDraftQuery): MutableList<StrictBrokerDraft> {
    return query.apply(store.list())
}
