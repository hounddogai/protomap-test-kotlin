package com.acme.transport.lastmile.redelivery.metric

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps default redelivery notices by code. */
interface DefaultRedeliveryNoticeStore {
    fun find(code: String): DefaultRedeliveryNotice?
    fun save(item: DefaultRedeliveryNotice)
    fun remove(code: String): Boolean
    fun list(): MutableList<DefaultRedeliveryNotice>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDefaultRedeliveryNoticeStore(
    var items: MutableMap<String, DefaultRedeliveryNotice>,
    var order: MutableList<String>,
) : DefaultRedeliveryNoticeStore {
    override fun find(code: String): DefaultRedeliveryNotice? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DefaultRedeliveryNotice) {
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

    override fun list(): MutableList<DefaultRedeliveryNotice> {
        val out = mutableListOf<DefaultRedeliveryNotice>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDefaultRedeliveryNoticeStore(): DefaultRedeliveryNoticeStore {
    return MemoryDefaultRedeliveryNoticeStore(
        mutableMapOf<String, DefaultRedeliveryNotice>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDefaultRedeliveryNoticeStore(
    var inner: DefaultRedeliveryNoticeStore,
    var cache: TtlCache<DefaultRedeliveryNotice>,
    var hits: Int,
    var misses: Int,
) : DefaultRedeliveryNoticeStore {
    override fun find(code: String): DefaultRedeliveryNotice? {
        val hit = (cache.get(code) as DefaultRedeliveryNotice?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DefaultRedeliveryNotice) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DefaultRedeliveryNotice> = inner.list()
}

fun newCachedDefaultRedeliveryNoticeStore(inner: DefaultRedeliveryNoticeStore, ttl: Int, clock: Clock): DefaultRedeliveryNoticeStore {
    return CachedDefaultRedeliveryNoticeStore(
        inner = inner,
        cache = newTtlCache<DefaultRedeliveryNotice>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects default redelivery notices. Empty fields match all. */
data class DefaultRedeliveryNoticeQuery(
    var name: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DefaultRedeliveryNotice): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
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

    fun apply(items: MutableList<DefaultRedeliveryNotice>): MutableList<DefaultRedeliveryNotice> {
        val matched = filter(items) { candidate: DefaultRedeliveryNotice -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDefaultRedeliveryNotices(store: DefaultRedeliveryNoticeStore, query: DefaultRedeliveryNoticeQuery): MutableList<DefaultRedeliveryNotice> {
    return query.apply(store.list())
}
