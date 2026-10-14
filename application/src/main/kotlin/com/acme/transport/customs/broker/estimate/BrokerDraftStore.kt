package com.acme.transport.customs.broker.estimate

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps broker drafts by code. */
interface BrokerDraftStore {
    fun find(code: String): BrokerDraft?
    fun save(item: BrokerDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<BrokerDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBrokerDraftStore(
    var items: MutableMap<String, BrokerDraft>,
    var order: MutableList<String>,
) : BrokerDraftStore {
    override fun find(code: String): BrokerDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BrokerDraft) {
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

    override fun list(): MutableList<BrokerDraft> {
        val out = mutableListOf<BrokerDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBrokerDraftStore(): BrokerDraftStore {
    return MemoryBrokerDraftStore(mutableMapOf<String, BrokerDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBrokerDraftStore(
    var inner: BrokerDraftStore,
    var cache: TtlCache<BrokerDraft>,
    var hits: Int,
    var misses: Int,
) : BrokerDraftStore {
    override fun find(code: String): BrokerDraft? {
        val hit = (cache.get(code) as BrokerDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BrokerDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BrokerDraft> = inner.list()
}

fun newCachedBrokerDraftStore(inner: BrokerDraftStore, ttl: Int, clock: Clock): BrokerDraftStore {
    return CachedBrokerDraftStore(
        inner = inner,
        cache = newTtlCache<BrokerDraft>(512, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects broker drafts. Empty fields match all. */
data class BrokerDraftQuery(
    var country: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BrokerDraft): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.expiresAt < minExpiresAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BrokerDraft>): MutableList<BrokerDraft> {
        val matched = filter(items) { candidate: BrokerDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBrokerDrafts(store: BrokerDraftStore, query: BrokerDraftQuery): MutableList<BrokerDraft> {
    return query.apply(store.list())
}
