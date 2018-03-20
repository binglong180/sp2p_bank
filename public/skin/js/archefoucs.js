﻿$(function () {
    var $con = $('#gg'), $box = $con.find('#img_box'), $btns = $con.find('#tab_btns'), i = 0, autoChange = function () {
        i += 1;
        if (i === 4) { i = 0; }
        $btns.find('a:eq(' + i + ')').addClass('ggOn').siblings().removeClass('ggOn');
        var curr = $box.find('a:eq(' + i + ')'), prev = curr.siblings();
        prev.css('z-index',2);
        curr.css('z-index',3).animate({
            'opacity': 1
        }, 150, function () {
            prev.css({
                'z-index':1, 'opacity': 0.1
            });
        });
    }, loop = setInterval(autoChange,4000);
    $con.hover(function () {
        clearInterval(loop);
    }, function () {
        loop = setInterval(autoChange,4000);
    });
    $btns.find('a').click(function () {
        i = $(this).index() - 1;
        autoChange();
    });
});