package com.acme.transport.customs.broker.forecast

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps default broker drafts by code. */
interface DefaultBrokerDraftStore {
    fun find(code: String): DefaultBrokerDraft?
    fun save(item: DefaultBrokerDraft)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultBrokerDraft>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultBrokerDraftStore(
    var items: MutableMap<String, DefaultBrokerDraft>,
    var order: MutableList<String>,
) : DefaultBrokerDraftStore {
    override fun find(code: String): DefaultBrokerDraft? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultBrokerDraft) {
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

    override fun list(): MutableList<DefaultBrokerDraft> {
        val out = mutableListOf<DefaultBrokerDraft>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultBrokerDraftStore(): DefaultBrokerDraftStore {
    return MemoryDefaultBrokerDraftStore(mutableMapOf<String, DefaultBrokerDraft>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultBrokerDraftStore(
    var inner: DefaultBrokerDraftStore,
    var cache: TtlCache<DefaultBrokerDraft>,
    var hits: Int,
    var misses: Int,
) : DefaultBrokerDraftStore {
    override fun find(code: String): DefaultBrokerDraft? {
        val hit = (cache.get(code) as DefaultBrokerDraft?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultBrokerDraft) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultBrokerDraft> = inner.list()
}

fun newCachedDefaultBrokerDraftStore(inner: DefaultBrokerDraftStore, ttl: Int, clock: Clock): DefaultBrokerDraftStore {
    return CachedDefaultBrokerDraftStore(
        inner = inner,
        cache = newTtlCache<DefaultBrokerDraft>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default broker drafts. Empty fields match all. */
data class DefaultBrokerDraftQuery(
    var label: String,
    var minOdometer: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultBrokerDraft): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<DefaultBrokerDraft>): MutableList<DefaultBrokerDraft> {
        val matched = filter(items) { candidate: DefaultBrokerDraft -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultBrokerDrafts(store: DefaultBrokerDraftStore, query: DefaultBrokerDraftQuery): MutableList<DefaultBrokerDraft> {
    return query.apply(store.list())
}
