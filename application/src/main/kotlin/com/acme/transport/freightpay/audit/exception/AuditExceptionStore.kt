package com.acme.transport.freightpay.audit.exception

import com.acme.transport.freightpay.cache.TtlCache
import com.acme.transport.freightpay.cache.newTtlCache
import com.acme.transport.freightpay.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps audit exceptions by code. */
interface AuditExceptionStore {
    fun find(code: String): AuditException?
    fun save(item: AuditException)
    fun remove(code: String): Boolean
    fun list(): MutableList<AuditException>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryAuditExceptionStore(
    var items: MutableMap<String, AuditException>,
    var order: MutableList<String>,
) : AuditExceptionStore {
    override fun find(code: String): AuditException? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: AuditException) {
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

    override fun list(): MutableList<AuditException> {
        val out = mutableListOf<AuditException>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryAuditExceptionStore(): AuditExceptionStore {
    return MemoryAuditExceptionStore(mutableMapOf<String, AuditException>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedAuditExceptionStore(
    var inner: AuditExceptionStore,
    var cache: TtlCache<AuditException>,
    var hits: Int,
    var misses: Int,
) : AuditExceptionStore {
    override fun find(code: String): AuditException? {
        val hit = (cache.get(code) as AuditException?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: AuditException) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<AuditException> = inner.list()
}

fun newCachedAuditExceptionStore(inner: AuditExceptionStore, ttl: Int, clock: Clock): AuditExceptionStore {
    return CachedAuditExceptionStore(
        inner = inner,
        cache = newTtlCache<AuditException>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects audit exceptions. Empty fields match all. */
data class AuditExceptionQuery(
    var name: String,
    var minExpiresAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: AuditException): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<AuditException>): MutableList<AuditException> {
        val matched = filter(items) { candidate: AuditException -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findAuditExceptions(store: AuditExceptionStore, query: AuditExceptionQuery): MutableList<AuditException> {
    return query.apply(store.list())
}
