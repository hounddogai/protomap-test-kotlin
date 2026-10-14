package com.acme.transport.freightpay.dispute.window

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps dispute exceptions by code. */
interface DisputeExceptionStore {
    fun find(code: String): DisputeException?
    fun save(item: DisputeException)
    fun remove(code: String): Boolean
    fun list(): MutableList<DisputeException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDisputeExceptionStore(
    var items: MutableMap<String, DisputeException>,
    var order: MutableList<String>,
) : DisputeExceptionStore {
    override fun find(code: String): DisputeException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DisputeException) {
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

    override fun list(): MutableList<DisputeException> {
        val out = mutableListOf<DisputeException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDisputeExceptionStore(): DisputeExceptionStore {
    return MemoryDisputeExceptionStore(mutableMapOf<String, DisputeException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDisputeExceptionStore(
    var inner: DisputeExceptionStore,
    var cache: TtlCache<DisputeException>,
    var hits: Int,
    var misses: Int,
) : DisputeExceptionStore {
    override fun find(code: String): DisputeException? {
        val hit = (cache.get(code) as DisputeException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DisputeException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DisputeException> = inner.list()
}

fun newCachedDisputeExceptionStore(inner: DisputeExceptionStore, ttl: Int, clock: Clock): DisputeExceptionStore {
    return CachedDisputeExceptionStore(
        inner = inner,
        cache = newTtlCache<DisputeException>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects dispute exceptions. Empty fields match all. */
data class DisputeExceptionQuery(
    var label: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DisputeException): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<DisputeException>): MutableList<DisputeException> {
        val matched = filter(items) { candidate: DisputeException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDisputeExceptions(store: DisputeExceptionStore, query: DisputeExceptionQuery): MutableList<DisputeException> {
    return query.apply(store.list())
}
