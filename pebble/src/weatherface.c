# Copyright (c) 2013 Rick Ucker
#
# Permission is hereby granted, free of charge, to any person obtaining a
# copy of this software and associated documentation files (the "Software"),
# to deal in the Software without restriction, including without limitation
# the rights to use, copy, modify, merge, publish, distribute, sublicense,
# and/or sell copies of the Software, and to permit persons to whom the
# Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
# THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
# FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
# DEALINGS IN THE SOFTWARE.

#include "pebble_os.h"
#include "pebble_app.h"
#include "pebble_fonts.h"


#define MY_UUID { 0xE2, 0x31, 0x01, 0xB0, 0xF4, 0x18, 0x4F, 0x72, 0x87, 0x09, 0xFC, 0x90, 0x79, 0x9F, 0xEE, 0x98 }
PBL_APP_INFO(MY_UUID,
             "Weatherface", "Rick Ucker",
             1, 0, /* App version */
             DEFAULT_MENU_ICON,
             APP_INFO_WATCH_FACE);

Window window;
TextLayer layerTime;
TextLayer layerWeather;

GSize layerWeatherMaxSize;

enum {
  ICON_KEY = 0x0,         // TUPLE_INT
  TEMP_KEY = 0x1,  // TUPLE_CSTRING
};


void handle_init(AppContextRef ctx) {

  window_init(&window, "Main");
  window_set_background_color(&window, GColorBlack);
  window_stack_push(&window, true /* Animated */);

  text_layer_init(&layerWeather, window.layer.frame);
  text_layer_set_background_color(&layerWeather, GColorClear);
  text_layer_set_text_color(&layerWeather, GColorWhite);
  text_layer_set_font(&layerWeather, fonts_get_system_font(FONT_KEY_ROBOTO_CONDENSED_21));
  layerWeatherMaxSize = GSize(144, 21);
  text_layer_set_size(&layerWeather, layerWeatherMaxSize);
  text_layer_set_text_alignment(&layerWeather, GTextAlignmentRight);
  layer_add_child(&window.layer, &layerWeather.layer);

  text_layer_init(&layerTime, window.layer.frame);
  text_layer_set_background_color(&layerTime, GColorClear);
  text_layer_set_text_color(&layerTime, GColorWhite);
  text_layer_set_font(&layerTime, fonts_get_system_font(FONT_KEY_ROBOTO_BOLD_SUBSET_49));
  layer_set_frame((Layer *)&layerTime, GRect(0, 22, 144, 146));
  layer_add_child(&window.layer, &layerTime.layer);
}

void handle_minute_tick(AppContextRef ctx, PebbleTickEvent *e) {
  static char timeText[] = "00:00";
  char *timeFormat = "%I:%M";

  string_format_time(timeText, sizeof(timeText), timeFormat, e->tick_time);

  text_layer_set_text(&layerTime, timeText);
}

void my_out_sent_handler(DictionaryIterator *sent, void *context) {
// outgoing message was delivered
}
void my_out_fail_handler(DictionaryIterator *failed, AppMessageResult reason, void *context) {
// outgoing message failed
}
void my_in_rcv_handler(DictionaryIterator *received, void *context) {
  // incoming message received
  Tuple *in_tuple = dict_find(received, TEMP_KEY);
  if (in_tuple)
    text_layer_set_text(&layerWeather, in_tuple->value->cstring);
}
void my_in_drp_handler(void *context, AppMessageResult reason) {
// incoming message dropped
}

void pbl_main(void *params) {
  static PebbleAppHandlers handlers = {
    .init_handler = &handle_init,
    .tick_info = {
      .tick_handler = &handle_minute_tick,
      .tick_units = MINUTE_UNIT
    },
    .messaging_info = {
	.buffer_sizes = {
    	.inbound = 64, // inbound buffer size in bytes
	.outbound = 16, // outbound buffer size in bytes
    	}, 
      .default_callbacks.callbacks = {
  	  .out_sent = my_out_sent_handler,
	  .out_failed = my_out_fail_handler,
	  .in_received = my_in_rcv_handler,
	  .in_dropped = my_in_drp_handler,
      },
    },
  };
  app_event_loop(params, &handlers);
}
