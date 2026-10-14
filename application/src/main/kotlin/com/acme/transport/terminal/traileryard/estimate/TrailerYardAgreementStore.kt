package com.acme.transport.terminal.traileryard.estimate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps trailer yard agreements by code. */
interface TrailerYardAgreementStore {
    fun find(code: String): TrailerYardAgreement?
    fun save(item: TrailerYardAgreement)
    fun remove(code: String): Boolean
    fun list(): MutableList<TrailerYardAgreement>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryTrailerYardAgreementStore(
    var items: MutableMap<String, TrailerYardAgreement>,
    var order: MutableList<String>,
) : TrailerYardAgreementStore {
    override fun find(code: String): TrailerYardAgreement? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: TrailerYardAgreement) {
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

    override fun list(): MutableList<TrailerYardAgreement> {
        val out = mutableListOf<TrailerYardAgreement>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryTrailerYardAgreementStore(): TrailerYardAgreementStore {
    return MemoryTrailerYardAgreementStore(
        mutableMapOf<String, TrailerYardAgreement>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedTrailerYardAgreementStore(
    var inner: TrailerYardAgreementStore,
    var cache: TtlCache<TrailerYardAgreement>,
    var hits: Int,
    var misses: Int,
) : TrailerYardAgreementStore {
    override fun find(code: String): TrailerYardAgreement? {
        val hit = (cache.get(code) as TrailerYardAgreement?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: TrailerYardAgreement) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<TrailerYardAgreement> = inner.list()
}

fun newCachedTrailerYardAgreementStore(inner: TrailerYardAgreementStore, ttl: Int, clock: Clock): TrailerYardAgreementStore {
    return CachedTrailerYardAgreementStore(
        inner = inner,
        cache = newTtlCache<TrailerYardAgreement>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects trailer yard agreements. Empty fields match all. */
data class TrailerYardAgreementQuery(
    var title: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: TrailerYardAgreement): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<TrailerYardAgreement>): MutableList<TrailerYardAgreement> {
        val matched = filter(items) { candidate: TrailerYardAgreement -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findTrailerYardAgreements(store: TrailerYardAgreementStore, query: TrailerYardAgreementQuery): MutableList<TrailerYardAgreement> {
    return query.apply(store.list())
}
